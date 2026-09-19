import { Firestore } from "firebase-admin/firestore";
import { Messaging } from "firebase-admin/messaging";
import { DEAD_TOKEN_CODES, PushPlan, toFcmMessage } from "./notifications";

export async function sendPlan(db: Firestore, messaging: Messaging, plan: PushPlan): Promise<void> {
  const tokens = await db.collection(`users/${plan.toUid}/fcmTokens`).get();
  if (tokens.empty) return;
  const messages = tokens.docs.map((d) => toFcmMessage(d.id, plan.data));
  const result = await messaging.sendEach(messages);
  const dead = result.responses
    .map((r, i) => (!r.success && r.error && DEAD_TOKEN_CODES.includes(r.error.code) ? tokens.docs[i] : null))
    .filter((d): d is NonNullable<typeof d> => d !== null);
  await Promise.all(dead.map((d) => d.ref.delete()));
}
