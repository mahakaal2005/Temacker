import { HandoffData, NUDGE_AFTER_MS, isNudgeDue, planForHandoffChange, planNudges, toFcmMessage } from "./notifications";

const ctx = { projectId: "p1", taskId: "t1", handoffId: "h1", taskTitle: "Sponsor deck" };
const offered: HandoffData = {
  fromUid: "a", fromDisplayName: "Ann", toUid: "b", toDisplayName: "Bo",
  note: "please", status: "OFFERED", offeredAt: 1_000_000
};

describe("planForHandoffChange", () => {
  test("a new OFFERED handoff pings the recipient", () => {
    const plan = planForHandoffChange(undefined, offered, ctx);
    expect(plan?.toUid).toBe("b");
    expect(plan?.data.type).toBe("HANDOFF_OFFERED");
    expect(plan?.data.taskTitle).toBe("Sponsor deck");
  });

  test("accept pings the offerer", () => {
    const plan = planForHandoffChange(offered, { ...offered, status: "ACCEPTED" }, ctx);
    expect(plan).toMatchObject({ toUid: "a", data: { type: "HANDOFF_ACCEPTED" } });
  });

  test("decline pings the offerer with the reason", () => {
    const plan = planForHandoffChange(offered, { ...offered, status: "DECLINED", declineReason: "away" }, ctx);
    expect(plan).toMatchObject({ toUid: "a", data: { type: "HANDOFF_DECLINED", declineReason: "away" } });
  });

  test("a nudgedAt-only update is silent", () => {
    expect(planForHandoffChange(offered, { ...offered, nudgedAt: 5 }, ctx)).toBeNull();
  });

  test("deletes and non-OFFERED creates are silent", () => {
    expect(planForHandoffChange(offered, undefined, ctx)).toBeNull();
    expect(planForHandoffChange(undefined, { ...offered, status: "ACCEPTED" }, ctx)).toBeNull();
  });
});

describe("nudges", () => {
  const now = offered.offeredAt + NUDGE_AFTER_MS;

  test("due exactly at the cutoff, not before", () => {
    expect(isNudgeDue(offered, now)).toBe(true);
    expect(isNudgeDue(offered, now - 1)).toBe(false);
  });

  test("not due once nudged or no longer OFFERED; missing nudgedAt counts as not nudged", () => {
    expect(isNudgeDue({ ...offered, nudgedAt: 1 }, now)).toBe(false);
    expect(isNudgeDue({ ...offered, status: "ACCEPTED" }, now)).toBe(false);
    expect(isNudgeDue({ ...offered, nudgedAt: null }, now)).toBe(true);
  });

  test("plans one push per side with hours waiting", () => {
    const plans = planNudges(offered, ctx, now + 60 * 60 * 1000);
    expect(plans.map((p) => p.toUid)).toEqual(["a", "b"]);
    expect(plans[0].data).toMatchObject({ type: "HANDOFF_NUDGE_OFFERER", hoursWaiting: "19" });
    expect(plans[1].data.type).toBe("HANDOFF_NUDGE_RECIPIENT");
  });
});

test("toFcmMessage is data-only with high priority", () => {
  const m = toFcmMessage("tok", { a: "b" });
  expect(m).toEqual({ token: "tok", data: { a: "b" }, android: { priority: "high" } });
  expect("notification" in m).toBe(false);
});
