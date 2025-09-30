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
          }
        ]
      }
    ]
  }
]);

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <AuthProvider>
      <RouterProvider router={router} />
      <Toaster position="top-right" reverseOrder={false} />
    </AuthProvider>
  </StrictMode>,
);
