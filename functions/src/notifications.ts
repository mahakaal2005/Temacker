export const NUDGE_AFTER_MS = 18 * 60 * 60 * 1000;

export type PushType =
  | "HANDOFF_OFFERED"
  | "HANDOFF_ACCEPTED"
  | "HANDOFF_DECLINED"
  | "HANDOFF_NUDGE_OFFERER"
  | "HANDOFF_NUDGE_RECIPIENT";

export interface HandoffData {
  fromUid: string;
  fromDisplayName: string;
  toUid: string;
  toDisplayName: string;
  note?: string | null;
  status: string;
  declineReason?: string | null;
  offeredAt: number;
  nudgedAt?: number | null;
}

export interface HandoffContext {
  projectId: string;
  taskId: string;
  handoffId: string;
  taskTitle: string;
}

export interface PushPlan {
  toUid: string;
  data: Record<string, string>;
}

const baseData = (type: PushType, h: HandoffData, ctx: HandoffContext): Record<string, string> => ({
  type,
  projectId: ctx.projectId,
  taskId: ctx.taskId,
  handoffId: ctx.handoffId,
  taskTitle: ctx.taskTitle,
  fromUid: h.fromUid,
  fromDisplayName: h.fromDisplayName,
  toUid: h.toUid,
  toDisplayName: h.toDisplayName,
  note: h.note ?? "",
  declineReason: h.declineReason ?? ""
});

// Offer → recipient; accept/decline → offerer; anything else (incl. the nudgedAt stamp) is silent.
export function planForHandoffChange(
  before: HandoffData | undefined,
  after: HandoffData | undefined,
  ctx: HandoffContext
): PushPlan | null {
  if (!after) return null;
  if (!before) {
    if (after.status !== "OFFERED") return null;
    return { toUid: after.toUid, data: baseData("HANDOFF_OFFERED", after, ctx) };
  }
  if (before.status !== "OFFERED" || before.status === after.status) return null;
  if (after.status === "ACCEPTED") {
    return { toUid: after.fromUid, data: baseData("HANDOFF_ACCEPTED", after, ctx) };
  }
  if (after.status === "DECLINED") {
    return { toUid: after.fromUid, data: baseData("HANDOFF_DECLINED", after, ctx) };
  }
  return null;
}

// Filtered in code, not `nudgedAt == null` in the query — that wouldn't match docs missing the field.
export function isNudgeDue(h: HandoffData, nowMillis: number): boolean {
  return h.status === "OFFERED" && h.nudgedAt == null && h.offeredAt <= nowMillis - NUDGE_AFTER_MS;
}

export function planNudges(h: HandoffData, ctx: HandoffContext, nowMillis: number): PushPlan[] {
  const hoursWaiting = String(Math.floor((nowMillis - h.offeredAt) / (60 * 60 * 1000)));
  return [
    { toUid: h.fromUid, data: { ...baseData("HANDOFF_NUDGE_OFFERER", h, ctx), hoursWaiting } },
    { toUid: h.toUid, data: { ...baseData("HANDOFF_NUDGE_RECIPIENT", h, ctx), hoursWaiting } }
  ];
}

export const DEAD_TOKEN_CODES = [
  "messaging/registration-token-not-registered",
  "messaging/invalid-registration-token"
];

export interface SendOutcome {
  success: boolean;
  error?: { code: string };
}

// Only "gone for good" codes prune a token; anything else (bad payload, quota) must keep it.
export function deadTokenIndexes(outcomes: SendOutcome[]): number[] {
  return outcomes.flatMap((o, i) => (!o.success && o.error && DEAD_TOKEN_CODES.includes(o.error.code) ? [i] : []));
}

export function toFcmMessage(token: string, data: Record<string, string>) {
  // Data-only + high priority so the app builds the notification (actions, styling).
  return { token, data, android: { priority: "high" as const } };
}
