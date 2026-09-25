import { planMemberRemoval } from "./memberRemoval";

const leader = { uid: "lead", displayName: "Lea Der" };

describe("planMemberRemoval", () => {
  test("unfinished tasks held by the departed go to the Leader; done ones stay as history", () => {
    const plan = planMemberRemoval(
      "gone",
      "Gone Person",
      leader,
      [
        { id: "t1", holderUid: "gone", status: "DOING" },
        { id: "t2", holderUid: "gone", status: "DONE" },
        { id: "t3", holderUid: "other", status: "TODO" }
      ],
      []
    );
    expect(plan.taskReassignments).toEqual([{ taskId: "t1", holderUid: "lead", holderDisplayName: "Lea Der" }]);
  });

  test("offers sent by or to the departed are declined with a reason; others are untouched", () => {
    const plan = planMemberRemoval("gone", "Gone Person", leader, [], [
      { taskId: "t1", handoffId: "h1", fromUid: "gone", toUid: "x" },
      { taskId: "t2", handoffId: "h2", fromUid: "y", toUid: "gone" },
      { taskId: "t3", handoffId: "h3", fromUid: "y", toUid: "x" }
    ]);
    expect(plan.handoffDeclines.map((d) => d.handoffId)).toEqual(["h1", "h2"]);
    expect(plan.handoffDeclines[0].reason).toBe("Gone Person left the project");
    expect(plan.handoffDeclines[0].closedForUid).toBe("gone");
  });

  test("with no Leader found, tasks are left alone but offers are still closed", () => {
    const plan = planMemberRemoval(
      "gone",
      "Gone Person",
      null,
      [{ id: "t1", holderUid: "gone", status: "TODO" }],
      [{ taskId: "t1", handoffId: "h1", fromUid: "gone", toUid: "x" }]
    );
    expect(plan.taskReassignments).toEqual([]);
    expect(plan.handoffDeclines).toHaveLength(1);
  });

  test("nothing to do for a member with no tasks or offers", () => {
    expect(planMemberRemoval("gone", "G", leader, [], [])).toEqual({ taskReassignments: [], handoffDeclines: [] });
  });
});
