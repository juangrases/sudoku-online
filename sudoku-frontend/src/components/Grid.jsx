import React from 'react';
class Grid extends React.Component {

	render() {
		const value = this.props.value === 0 ? "" : this.props.value
		return(
			<input type="text" value={value} onChange={this.props.onChange}/>
		)
	}

}

export default Grid;