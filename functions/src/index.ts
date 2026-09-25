import { initializeApp } from "firebase-admin/app";
import { getFirestore } from "firebase-admin/firestore";
import { getMessaging } from "firebase-admin/messaging";
import { setGlobalOptions } from "firebase-functions/v2";
import * as logger from "firebase-functions/logger";
import { onDocumentDeleted, onDocumentWritten } from "firebase-functions/v2/firestore";
import { onSchedule } from "firebase-functions/v2/scheduler";
import { HandoffData, isNudgeDue, planForHandoffChange, planNudges } from "./notifications";
import { Leader, OfferedHandoff, TaskInfo, planMemberRemoval } from "./memberRemoval";
import { sendPlan } from "./send";

// Firestore is nam5 (multi-region US), whose trigger region is us-central1.
setGlobalOptions({ region: "us-central1" });
initializeApp();

const HANDOFF_PATH = "projects/{projectId}/tasks/{taskId}/handoffs/{handoffId}";

async function taskTitle(projectId: string, taskId: string): Promise<string | null> {
  const snap = await getFirestore().doc(`projects/${projectId}/tasks/${taskId}`).get();
  return snap.exists ? ((snap.get("title") as string | undefined) ?? "a task") : null;
}

// A missing name just leaves the notification without a project label.
async function projectName(projectId: string): Promise<string> {
  const snap = await getFirestore().doc(`projects/${projectId}`).get();
  return (snap.get("name") as string | undefined) ?? "";
}

export const onHandoffWritten = onDocumentWritten(HANDOFF_PATH, async (event) => {
  const change = event.data;
  if (!change) return;
  const { projectId, taskId, handoffId } = event.params;
  const before = change.before.exists ? (change.before.data() as HandoffData) : undefined;
  const after = change.after.exists ? (change.after.data() as HandoffData) : undefined;
  const title = await taskTitle(projectId, taskId);
  if (title === null) return; // task deleted — nothing to point the push at
  const plan = planForHandoffChange(before, after, {
    projectId,
    projectName: await projectName(projectId),
    taskId,
    handoffId,
    taskTitle: title
  });
  if (plan) await sendPlan(getFirestore(), getMessaging(), plan);
});

async function findLeader(projectId: string): Promise<Leader | null> {
  const members = getFirestore().collection(`projects/${projectId}/members`);
  // Pre-Phase-4 member docs have no isLeader field, so fall back to the Leader role name.
  let snap = await members.where("isLeader", "==", true).limit(1).get();
  if (snap.empty) snap = await members.where("roleName", "==", "Leader").limit(1).get();
  const doc = snap.docs[0];
  return doc ? { uid: doc.id, displayName: (doc.get("displayName") as string | undefined) ?? "the Leader" } : null;
}

// A member leaving or being removed (including account deletion) must not strand their work: unfinished
// tasks go to the Leader and every open offer to or from them is closed as a decline.
export const onMemberRemoved = onDocumentDeleted("projects/{projectId}/members/{userId}", async (event) => {
  const { projectId, userId } = event.params;
  const db = getFirestore();
  const displayName = (event.data?.get("displayName") as string | undefined) ?? "A member";

  const taskDocs = (await db.collection(`projects/${projectId}/tasks`).get()).docs;
  const tasks: TaskInfo[] = taskDocs.map((d) => ({
    id: d.id,
    holderUid: d.get("holderUid") as string,
    status: (d.get("status") as string | undefined) ?? "TODO"
  }));
  const offered: OfferedHandoff[] = [];
  for (const taskDoc of taskDocs) {
    const open = await taskDoc.ref.collection("handoffs").where("status", "==", "OFFERED").get();
    for (const h of open.docs) {
      offered.push({ taskId: taskDoc.id, handoffId: h.id, fromUid: h.get("fromUid") as string, toUid: h.get("toUid") as string });
    }
  }

  const plan = planMemberRemoval(userId, displayName, await findLeader(projectId), tasks, offered);
  const now = Date.now();
  const ops: ((b: FirebaseFirestore.WriteBatch) => void)[] = [
    ...plan.taskReassignments.map((t) => (b: FirebaseFirestore.WriteBatch) =>
      b.update(db.doc(`projects/${projectId}/tasks/${t.taskId}`), {
        holderUid: t.holderUid,
        holderDisplayName: t.holderDisplayName,
        updatedAt: now
      })
    ),
    ...plan.handoffDeclines.map((d) => (b: FirebaseFirestore.WriteBatch) =>
      b.update(db.doc(`projects/${projectId}/tasks/${d.taskId}/handoffs/${d.handoffId}`), {
        status: "DECLINED",
        declineReason: d.reason,
        respondedAt: now,
        closedForUid: d.closedForUid
      })
    )
  ];
  // Batches are capped at 500 writes; commit in chunks.
  for (let i = 0; i < ops.length; i += 400) {
    const batch = db.batch();
    ops.slice(i, i + 400).forEach((op) => op(batch));
    await batch.commit();
  }
  logger.info("Member removal cleanup", {
    projectId,
    userId,
    tasksReassigned: plan.taskReassignments.length,
    offersClosed: plan.handoffDeclines.length
  });
});

// Runs every 30 minutes; nudgedAt makes each stale handoff fire exactly once.
export const nudgeStaleHandoffs = onSchedule("every 30 minutes", async () => {
  const db = getFirestore();
  const now = Date.now();
  const stale = await db
    .collectionGroup("handoffs")
    .where("status", "==", "OFFERED")
    .where("offeredAt", "<=", now - 18 * 60 * 60 * 1000)
    .get();
  for (const doc of stale.docs) {
    const handoff = doc.data() as HandoffData;
    if (!isNudgeDue(handoff, now)) continue;
    const taskRef = doc.ref.parent.parent;
    const projectRef = taskRef?.parent.parent;
    if (!taskRef || !projectRef) continue;
    const title = await taskTitle(projectRef.id, taskRef.id);
    if (title === null) continue;
    const ctx = {
      projectId: projectRef.id,
      projectName: await projectName(projectRef.id),
      taskId: taskRef.id,
      handoffId: doc.id,
      taskTitle: title
    };
    await doc.ref.update({ nudgedAt: now }); // stamp first so a send failure can't re-nudge every run
    for (const plan of planNudges(handoff, ctx, now)) await sendPlan(db, getMessaging(), plan);
  }
});
