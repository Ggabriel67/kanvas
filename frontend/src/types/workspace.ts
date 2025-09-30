export interface WorkspaceProjection {
    id: number;
    name: string;
    role: string;
};

export interface Workspace {
    id: number;
    name: string;
    description: string;
    createdAt: string;
    workspaceRole: string;
    boardProjections: BoardProjection[];
};

export interface BoardProjection {
    boardId: number;
    name: string
};
