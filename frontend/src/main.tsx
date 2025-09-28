import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { createBrowserRouter, RouterProvider } from 'react-router-dom'
import './index.css'
import SignUp from './pages/SignUp.tsx'
import NotFoundPage from './pages/NotFoundPage.tsx'
import SignIn from './pages/SignIn.tsx'

const router = createBrowserRouter([
  {
    path: '/',
    element: <SignIn />,
    errorElement: <NotFoundPage />
  },
  {
    path: '/signup',
    element: <SignUp />,
  }
]);

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <RouterProvider router={router} />
  </StrictMode>,
);
