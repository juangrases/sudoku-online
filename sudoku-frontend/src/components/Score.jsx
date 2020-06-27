import React from 'react'

const Score = ({allMembers, scores}) => {
	return (
		<div>
			<h2>Current members:</h2>
			<ul>
				{allMembers.map(name => {
					const score = scores[name]
					const computedScore = score ? score : {}
					return (<li key={name}>{name} - {computedScore.successes}/{computedScore.wrongs}</li>)
				})}
			</ul>
		</div>
	)
}

export default Score