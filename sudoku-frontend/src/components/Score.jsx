import React from 'react'

const Score = ({scores}) => {
	console.log(scores)
	return (
		<div>
			<h2>Current members:</h2>
			<ul>
				{Object.entries(scores).map(([name, score]) => {
					const computedScore = score ? score : {}
					return (<li key={name}>{name} - {computedScore.successes}/{computedScore.wrongs}</li>)
				})}
			</ul>
		</div>
	)
}

export default Score