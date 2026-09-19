import { initializeApp } from "firebase-admin/app";
import { getFirestore } from "firebase-admin/firestore";
import { getMessaging } from "firebase-admin/messaging";
import { setGlobalOptions } from "firebase-functions/v2";
import { onDocumentWritten } from "firebase-functions/v2/firestore";
import { onSchedule } from "firebase-functions/v2/scheduler";
import { HandoffData, isNudgeDue, planForHandoffChange, planNudges } from "./notifications";
import { sendPlan } from "./send";

// Firestore is nam5 (multi-region US), whose trigger region is us-central1.
setGlobalOptions({ region: "us-central1" });
initializeApp();

const HANDOFF_PATH = "projects/{projectId}/tasks/{taskId}/handoffs/{handoffId}";

async function taskTitle(projectId: string, taskId: string): Promise<string | null> {
  const snap = await getFirestore().doc(`projects/${projectId}/tasks/${taskId}`).get();
  return snap.exists ? ((snap.get("title") as string | undefined) ?? "a task") : null;
}

export const onHandoffWritten = onDocumentWritten(HANDOFF_PATH, async (event) => {
  const change = event.data;
  if (!change) return;
  const { projectId, taskId, handoffId } = event.params;
  const before = change.before.exists ? (change.before.data() as HandoffData) : undefined;
  const after = change.after.exists ? (change.after.data() as HandoffData) : undefined;
  const title = await taskTitle(projectId, taskId);
  if (title === null) return; // task deleted — nothing to point the push at
  const plan = planForHandoffChange(before, after, { projectId, taskId, handoffId, taskTitle: title });
  if (plan) await sendPlan(getFirestore(), getMessaging(), plan);
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
    const ctx = { projectId: projectRef.id, taskId: taskRef.id, handoffId: doc.id, taskTitle: title };
    await doc.ref.update({ nudgedAt: now }); // stamp first so a send failure can't re-nudge every run
    for (const plan of planNudges(handoff, ctx, now)) await sendPlan(db, getMessaging(), plan);
  }
});
