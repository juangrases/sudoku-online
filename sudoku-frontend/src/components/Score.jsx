import React from 'react'

const Score = ({scores}) => {
	return (
		<div>
			<h2>Current members:</h2>
			<ul>
				{Object.entries(scores).map(([name, score]) => {
					return (<li key={name}>{name} - {score.successes}/{score.wrongs}</li>)
				})}
			</ul>
		</div>
	)
}

export default Score