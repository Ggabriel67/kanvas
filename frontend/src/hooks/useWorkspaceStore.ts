import { create } from "zustand";

type WorkspaceStore = {
  refreshKey: number;
	triggerSideBarRefresh: () => void;
};

export const useWorkspaceStore = create<WorkspaceStore>((set) => ({
	refreshKey: 0,
	triggerSideBarRefresh: () => set((state) => ({ refreshKey: state.refreshKey + 1 })),
}));
