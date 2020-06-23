import React from 'react'

const Grid = ({value, onChange, rowIndex, columnIndex, editable}) => {
	const computedValue = value === 0 ? '' : value
	//TODO: research BEM
	//TODO: Logic to

	const borderWidth = 2

	const computeBorder = () => {
		//Given a row and a column, know which borders need extra width
		let styles = {borderWidth: 1, borderStyle: "double"}
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
			<input style={{width: 30, height: 30, ...computeBorder(), borderRadius: 0, padding: 0, textAlign: "center"}}
						 type="text"
						 disabled={!editable}
						 value={computedValue}
						 onChange={onChange}
			/>
	)

}

export default Grid