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
		allMembers: [],
	}

	handleChange = (event) => {
		this.setState({name: event.target.value})
	}

	handleSubmit = (event) => {
		event.preventDefault()
		socket = new WebSocket('ws://192.168.1.16:8080/game?name=' + this.state.name)
		socket.onmessage = (event) => {
			console.log("received message")
			const message = event.data
			const json = JSON.parse(message)
			if (json.sudoku) {
				const newSudoku = json.sudoku
				const scores = json.scores
				this.setState(({sudoku}) => {
					if(JSON.stringify(newSudoku) === JSON.stringify(sudoku)){
						console.log("Good move!")
					}else{
						console.log("Bad move")
					}
					return {sudoku: newSudoku, scores}
				})
			}
			if (json.allMembers) {
				const allMembers = json.allMembers
				this.setState({allMembers})
			}
		}
		this.setState({isNameSet: true})
	}

	changeValue = (rowIndex, columnIndex) => (event) => {
		console.log("change value")
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

	render () {
		const {isNameSet, name, sudoku, allMembers, scores} = this.state
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
				<Sudoku sudoku={sudoku}
								changeValue={this.changeValue}/>

				<Score allMembers={allMembers} scores={scores}/>
			</div>
		)

	}
}

export default App
