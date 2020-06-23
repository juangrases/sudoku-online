import React from 'react'
import Grid from './Grid'

const socket = new WebSocket('ws://192.168.1.16:8080/game')
console.log('When this renders')

class Sudoku extends React.Component {

	state = {
		sudoku: [
			[{value: 0, editable: true}, {value: 3, editable: false}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}],
			[{value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}],
			[{value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}],
			[{value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}],
			[{value: 0, editable: true}, {value: 0, editable: true}, {value: 4, editable: false}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}],
			[{value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}],
			[{value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}],
			[{value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}],
			[{value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}, {value: 0, editable: true}]]
	}

	componentDidMount () {
		console.log('when componentDidMount renders')
		socket.onmessage = (event) => {
			const message = event.data
			const sudoku = JSON.parse(message)
			this.setState({sudoku})
		}
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