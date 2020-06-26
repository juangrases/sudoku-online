import React from 'react'
import Grid from './Grid'
import Welcome from './Welcome'

let socket = null

class Sudoku extends React.Component {

	state = {
		name: '',
		isNameSet: false,
		allMembers: [],
	}

	componentDidMount () {

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

	handleChange = (event) => {
		this.setState({name: event.target.value})
	}

	handleSubmit = (event) => {
		event.preventDefault()
		socket = new WebSocket('ws://192.168.1.16:8080/game?name=' + this.state.name)
		socket.onmessage = (event) => {
			const message = event.data
			const json = JSON.parse(message)
			if (json.sudoku) {
				const sudoku = json.sudoku
				this.setState({sudoku})
			}
			if (json.allMembers) {
				const allMembers = json.allMembers
				this.setState({allMembers})
			}
		}
		this.setState({isNameSet: true})
	}

	render () {
		if (!this.state.isNameSet) {
			return (
				<Welcome name={this.state.name} handleSubmit={this.handleSubmit} handleChange={this.handleChange}/>
			)
		}
		if(!this.state.sudoku){
			return null
		}
		return (
			<div>
				<div className="sudokuBoard" style={{textAlign: 'center', marginTop: 50}}>
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
				<div>
					<h2>Current members:</h2>
					<ul>
						{this.state.allMembers.map(name => <li key={name}>{name}</li>)}
					</ul>
				</div>
			</div>
		)
	}
}

export default Sudoku