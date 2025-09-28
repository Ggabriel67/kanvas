import React from 'react'
import { Link } from 'react-router-dom';

const NotFoundPage = () => {
  return (
		<div> 404 Not Found
			<Link to={"/"}> Go back to signin </Link>
		</div>
  )
}

export default NotFoundPage;
