import { Firestore } from "firebase-admin/firestore";
import { Messaging } from "firebase-admin/messaging";
import * as logger from "firebase-functions/logger";
import { PushPlan, deadTokenIndexes, toFcmMessage } from "./notifications";

export async function sendPlan(db: Firestore, messaging: Messaging, plan: PushPlan): Promise<void> {
  const tokens = await db.collection(`users/${plan.toUid}/fcmTokens`).get();
  if (tokens.empty) return;
  const result = await messaging.sendEach(tokens.docs.map((d) => toFcmMessage(d.id, plan.data)));
  result.responses.forEach((r, i) => {
    if (!r.success) logger.warn("FCM send failed", { uid: plan.toUid, type: plan.data.type, code: r.error?.code, tokenIndex: i });
  });
  const dead = deadTokenIndexes(result.responses).map((i) => tokens.docs[i]);
  await Promise.all(dead.map((d) => d.ref.delete()));
  logger.info("FCM sent", { uid: plan.toUid, type: plan.data.type, ok: result.successCount, failed: result.failureCount, pruned: dead.length });
}
