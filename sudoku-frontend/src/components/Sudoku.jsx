import React from 'react'
import Grid from './Grid'

const Sudoku = ({sudoku, changeValue}) => {
	return (
		<div>
			<div className="sudokuBoard" style={{textAlign: 'center', marginTop: 50}}>
				{sudoku.map((row, rowIndex) =>
					<div key={rowIndex} className="Row">
						{row.map((r, columnIndex) =>
							<Grid key={rowIndex * columnIndex + columnIndex}
										value={r.value}
										editable={r.editable}
										rowIndex={rowIndex}
										columnIndex={columnIndex}
										onChange={changeValue(rowIndex, columnIndex)}
							/>
						)}
					</div>
				)}
			</div>
		</div>
	)

}

export default Sudoku