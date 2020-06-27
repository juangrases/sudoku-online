import React from 'react'

const Grid = ({value, onChange, rowIndex, columnIndex, editable}) => {

	const computedValue = !isNaN(value) && parseInt(value) > 0 && parseInt(value) < 10 ? value : ''
	//TODO: research BEM
	//TODO: Logic to

	const borderWidth = 2

	const computeBorder = () => {
		//Given a row and a column, know which borders need extra width
		let styles = {borderWidth: 1}
		if(rowIndex % 3 === 0){
			styles = {...styles, borderTopWidth: borderWidth}

		}
		if(rowIndex === 8){
			styles = {...styles, borderBottomWidth: borderWidth}
		}
		if(columnIndex % 3 === 0){
			styles = {...styles, borderLeftWidth: borderWidth}

		}
		if(columnIndex === 8){
			styles = {...styles, borderRightWidth: borderWidth}
		}
		return styles
	}

	return (
			<input style={{...computeBorder(), width: 60, height: 60, borderRadius: 0, padding: 0, textAlign: "center", fontSize: 30}}
						 type="text"
						 disabled={!editable}
						 value={computedValue}
						 onChange={onChange}
			/>
	)

}

export default Grid