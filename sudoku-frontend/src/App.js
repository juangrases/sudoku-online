import React from 'react'
import './App.css'
import Welcome from './components/Welcome'
import Sudoku from './components/Sudoku'
import Score from './components/Score'

let socket = null

class App extends React.Component {
	state = {
		name: '',
		isNameSet: false,
	}

	handleChange = (event) => {
		this.setState({name: event.target.value})
	}

	handleSubmit = (event) => {
		event.preventDefault()
		socket = new WebSocket('ws://192.168.1.16:8080/game?name=' + this.state.name)
		socket.onmessage = (event) => {
			console.log('received message')
			const message = event.data
			const json = JSON.parse(message)
			const newSudoku = json.sudoku
			const currentTurn = json.currentTurn
			const scores = json.scores
			this.setState(({sudoku}) => {
				if (JSON.stringify(newSudoku) === JSON.stringify(sudoku)) {
					console.log('Good move!')
				} else {
					console.log('Bad move')
				}
				return {sudoku: newSudoku, scores, currentTurn}
			})

		}
		this.setState({isNameSet: true})
	}

	changeValue = (rowIndex, columnIndex) => (event) => {
		console.log('change value')
		const value = event.target.value
		if (value !== '') {
			this.setState(state => {
				state.sudoku[rowIndex][columnIndex] = {value, editable: true}
				return {
					sudoku: state.sudoku
				}
			}, () => {
				socket.send(JSON.stringify({row: rowIndex, col: columnIndex, value: value}))
			})
		}
	}

	render () {
		const {isNameSet, name, sudoku, scores, currentTurn} = this.state
		if (!isNameSet) {
			return (
				<Welcome name={name} handleSubmit={this.handleSubmit} handleChange={this.handleChange}/>
			)
		}
		if (!sudoku) {
			return null
		}
		return (
			<div>
				<h1 style={{textAlign: 'center'}}>{currentTurn} has the current turn</h1>
				<Sudoku sudoku={sudoku}
								changeValue={this.changeValue}/>

				<Score scores={scores}/>
			</div>
		)

	}
}

export default App
