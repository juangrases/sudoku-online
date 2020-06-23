import React from 'react'
import Grid from './Grid'

const socket = new WebSocket('ws://localhost:8080/game');
console.log("When this renders")
class Sudoku extends React.Component {


	state = {
		sudoku: [
			[0, 0, 0, 0, 0, 0, 0, 0, 0],
			[0, 0, 0, 0, 0, 0, 0, 0, 0],
			[0, 0, 0, 0, 0, 0, 0, 0, 0],
			[0, 0, 0, 0, 0, 0, 0, 0, 0],
			[0, 0, 0, 0, 0, 0, 0, 0, 0],
			[0, 0, 0, 0, 0, 0, 0, 0, 0],
			[0, 0, 0, 0, 0, 0, 0, 0, 0],
			[0, 0, 0, 0, 0, 0, 0, 0, 0],
			[0, 0, 0, 0, 0, 0, 0, 0, 0]]
	}

	componentDidMount(){
		console.log("when componentDidMount renders")
		socket.onmessage = (event) => {
			const message = event.data;
			console.log("received  a message "+message)
			const sudoku = JSON.parse(message)
			this.setState({sudoku})
		};
	}


	changeValue = (rowIndex, columnIndex) => (event) => {
		const value = event.target.value
		this.setState(state => {
			state.sudoku[rowIndex][columnIndex] = value
			return{
				sudoku: state.sudoku
			}
		}, () => {
			console.log("Sending to socket")
			socket.send(JSON.stringify(this.state.sudoku))
		})
	}

	render () {
		return (
			<div className="shopping-list">
				{this.state.sudoku.map((row, rowIndex) =>
					row.map((r, columnIndex) =>
						<Grid key={rowIndex * columnIndex + columnIndex}  value={r} rowIndex={rowIndex} rowColumn={columnIndex} onChange={this.changeValue(rowIndex, columnIndex)}/>
					))
				}
			</div>
		)
	}
}

export default Sudoku