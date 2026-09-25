export interface TaskInfo {
  id: string;
  holderUid: string;
  status: string;
}

export interface OfferedHandoff {
  taskId: string;
  handoffId: string;
  fromUid: string;
  toUid: string;
}

export interface Leader {
  uid: string;
  displayName: string;
}

export interface CleanupPlan {
  taskReassignments: { taskId: string; holderUid: string; holderDisplayName: string }[];
  handoffDeclines: { taskId: string; handoffId: string; reason: string; closedForUid: string }[];
}

// When a member leaves or is removed: their unfinished tasks go to the Leader, and every open offer
// they sent or were sent is closed as a decline so nothing waits on someone who is gone. Done tasks
// keep their holder as history.
export function planMemberRemoval(
  userId: string,
  displayName: string,
  leader: Leader | null,
  tasks: TaskInfo[],
  offered: OfferedHandoff[]
): CleanupPlan {
  const taskReassignments =
    leader && leader.uid !== userId
      ? tasks
          .filter((t) => t.holderUid === userId && t.status !== "DONE")
          .map((t) => ({ taskId: t.id, holderUid: leader.uid, holderDisplayName: leader.displayName }))
      : [];
  const handoffDeclines = offered
    .filter((h) => h.fromUid === userId || h.toUid === userId)
    .map((h) => ({
      taskId: h.taskId,
      handoffId: h.handoffId,
      reason: `${displayName} left the project`,
      closedForUid: userId
    }));
  return { taskReassignments, handoffDeclines };
}
