import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { createBrowserRouter, RouterProvider } from 'react-router-dom'
import './index.css'
import SignUp from './pages/SignUp.tsx'
import NotFoundPage from './pages/NotFoundPage.tsx'
import SignIn from './pages/SignIn.tsx'
import { Toaster } from 'react-hot-toast'
import AuthProvider from './providers/AuthProvider.tsx'
import PrivateRoute from './components/PrivateRoute.tsx'
import AppLayout from './components/layouts/AppLayout.tsx'
import Home from './pages/Home.tsx'
import Workspace from './pages/Workspace.tsx'
import WebSocketProvider from './providers/WebSocketProvider.tsx'
import GuestWorkspaces from './pages/GuestWorkspaces.tsx'
import BoardLayout from './components/layouts/BoardLayout.tsx'
import Board from './pages/Board.tsx'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'

const router = createBrowserRouter([
  {
    path: '/',
    element: <SignIn />,
    errorElement: <NotFoundPage />
  },
  {
    path: '/signup',
    element: <SignUp />,
  },
  {
    element: <PrivateRoute />,
    children: [
      {
        path: '/app',
        element: <AppLayout />,
        children: [
          {
            index: true,
            element: <Home />,
          },
          {
            path: "workspaces/:workspaceId",
            element: <Workspace />
          },
          {
            path: "guest-workspaces",
            element: <GuestWorkspaces />
          },
        ]
      },
      {
        path: '/app/boards',
        element: <BoardLayout />,
        children: [
          {
            path: ":boardId",
            element: <Board />
          },
        ]
      }
    ]
  }
]);

const queryClient = new QueryClient()

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <AuthProvider>
      <WebSocketProvider>
        <QueryClientProvider client={queryClient}>
          <RouterProvider router={router} />
          <Toaster position="top-right" reverseOrder={false} />
        </QueryClientProvider>
      </WebSocketProvider>   
    </AuthProvider>
  </StrictMode>,
);
