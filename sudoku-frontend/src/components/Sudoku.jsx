import React from 'react'
import Grid from './Grid'

const socket = new WebSocket('ws://192.168.1.16:8080/game')

class Sudoku extends React.Component {

	/*
	val sudoku: Sudoku = Array(
      Array(None, None, None, None, None, None, None, None, Some(8)),
      Array(Some(7), None, None, None, None, Some(4), None, Some(3), None),
      Array(None, Some(4), None, None, None, Some(3), Some(2), None, None),
      Array(Some(2), None, None, Some(3), Some(9), None, Some(8), None, Some(4)),
      Array(None, None, Some(7), Some(8), Some(2), None, None, Some(6), Some(3)),
      Array(None, Some(5), None, None, Some(7), Some(6), None, Some(9), Some(2)),
      Array(None, Some(7), Some(4), Some(2), Some(6), None, None, None, None),
      Array(None, Some(3), None, None, None, None, Some(6), Some(8), None),
      Array(Some(5), None, Some(6), None, None, Some(9), None, None, Some(7))
    )

	 */
	state = {
		sudoku: [
			[{value: "", editable: true}, {value: "", editable: true}, {value: "", editable: true}, {value: "", editable: true}, {value: "", editable: true}, {value: "", editable: true}, {value: "", editable: true}, {value: "", editable: true}, {value: "8", editable: false}],
			[{value: "7", editable: false}, {value: "", editable: true}, {value: "", editable: true}, {value: "", editable: true}, {value: "", editable: true}, {value: "4", editable: false}, {value: "", editable: true}, {value: "3", editable: false}, {value: "", editable: true}],
			[{value: "", editable: true}, {value: "4", editable: false}, {value: "", editable: true}, {value: "", editable: true}, {value: "", editable: true}, {value: "3", editable: false}, {value: "2", editable: false}, {value: "", editable: true}, {value: "", editable: true}],
			[{value: "2", editable: false}, {value: "", editable: true}, {value: "", editable: true}, {value: "3", editable: false}, {value: "9", editable: false}, {value: "", editable: true}, {value: "8", editable: false}, {value: "", editable: true}, {value: "4", editable: false}],
			[{value: "", editable: true}, {value: "", editable: true}, {value: "7", editable: false}, {value: "8", editable: false}, {value: "2", editable: false}, {value: "", editable: true}, {value: "", editable: true}, {value: "6", editable: false}, {value: "3", editable: false}],
			[{value: "", editable: true}, {value: "5", editable: false}, {value: "", editable: true}, {value: "", editable: true}, {value: "7", editable: false}, {value: "6", editable: false}, {value: "", editable: true}, {value: "9", editable: false}, {value: "2", editable: false}],
			[{value: "", editable: true}, {value: "7", editable: false}, {value: "4", editable: false}, {value: "2", editable: false}, {value: "6", editable: false}, {value: "", editable: true}, {value: "", editable: true}, {value: "", editable: true}, {value: "", editable: true}],
			[{value: "", editable: true}, {value: "3", editable: false}, {value: "", editable: true}, {value: "", editable: true}, {value: "", editable: true}, {value: "", editable: true}, {value: "6", editable: false}, {value: "8", editable: false}, {value: "", editable: true}],
			[{value: "5", editable: false}, {value: "", editable: true}, {value: "", editable: true}, {value: "", editable: true}, {value: "", editable: true}, {value: "9", editable: false}, {value: "", editable: true}, {value: "", editable: true}, {value: "7", editable: false}]]
	}

	componentDidMount () {
		socket.onmessage = (event) => {
			const message = event.data
			const sudoku = JSON.parse(message)
			this.setState({sudoku})
		}
		// socket.onopen = () =>{
		// 	socket.send(JSON.stringify(this.state.sudoku))
		// }
	}

	changeValue = (rowIndex, columnIndex) => (event) => {
		const value = event.target.value
		this.setState(state => {
			state.sudoku[rowIndex][columnIndex] = {value, editable: true}
			return {
				sudoku: state.sudoku
			}
		}, () => {
			socket.send(JSON.stringify(this.state.sudoku))
		})
	}

	render() {
		return (
			<div className="sudokuBoard" style={{textAlign: "center", marginRight: "auto"}}>
				{this.state.sudoku.map((row, rowIndex) =>
						<div key={rowIndex} className="Row">
							{row.map((r, columnIndex) =>
								<Grid key={rowIndex * columnIndex + columnIndex}
											value={r.value}
											editable={r.editable}
											rowIndex={rowIndex}
											columnIndex={columnIndex}
											onChange={this.changeValue(rowIndex, columnIndex)}
								/>
							)}
						</div>
				)}
			</div>
		)
	}
}

export default Sudoku