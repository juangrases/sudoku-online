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
		currentTime: 30,
		lastMoveWithSuccess: false
	}

	TURN_TIME = 45

	startTimer (remainingTime) {
		this.stopTimer()
		this.setState({remaining: remainingTime})
		this.timer = setInterval(() => this.setState(({remaining}) => {
			return {remaining: remaining === 0 ? this.TURN_TIME : remaining - 1}
		}), 1000)
	}

	stopTimer () {
		clearInterval(this.timer)
	}

	resetTimer () {
		this.setState({remaining: this.TURN_TIME})
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
			const {sudoku, currentTurn, scores, lastStartTime} = json
			// const newSudoku = json.sudoku
			// const currentTurn = json.currentTurn
			// const scores = json.scores
			this.setState((oldState) => {
				const goodMove = JSON.stringify(sudoku) === JSON.stringify(oldState.sudoku)

				if (oldState.currentTurn !== currentTurn) {
					this.resetTimer()
				}

				//Question: When to start the timer
				const remaining = this.TURN_TIME - parseInt((Date.now() - lastStartTime) / 1000, 10)
				this.startTimer(remaining)


				return {sudoku: sudoku, scores, currentTurn, remaining}
			})

		}

		this.setState({isNameSet: true})
	}

	changeValue = (rowIndex, columnIndex) => (event) => {
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
		const {isNameSet, name, sudoku, scores, currentTurn, remaining} = this.state
		if (!isNameSet) {
			return (
				<Welcome name={name} handleSubmit={this.handleSubmit} handleChange={this.handleChange}/>
			)
		}
		if (!sudoku) {
			return null
		}
		return (
			<div disabled={currentTurn !== name}>
				<h1 style={{textAlign: 'center'}}>{currentTurn} has the current turn - Time remaining {remaining}</h1>
				<Sudoku sudoku={sudoku}
								changeValue={this.changeValue}/>

				<Score scores={scores}/>
			</div>
		)

	}
}

export default App
