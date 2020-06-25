import React from 'react'

const Welcome = ({name, handleChange, handleSubmit}) => {
	return (
		<form style={{textAlign: 'center', marginTop: 50}} onSubmit={handleSubmit}>
			<label>
				Welcome! What's your name?
				<br/>
				<input value={name} type="text" name="name" onChange={handleChange}/>
			</label>
			<input type="submit" value="Submit"/>
		</form>
	)
}

export default Welcome