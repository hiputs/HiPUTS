import React, { Component } from 'react'


interface IProps{
    
}
export default class Navbar extends Component<IProps> {
  render() {
    return (
        <div className='navbar'>
            <label>HIPUTS visualization</label>
            <span className='buttons'>
                <button id = "resizeButton">Resize</button>
                <button id = "stopButton">Stop</button>
                <button id = "stepButton">Step</button>
                <button id = "playButton">Play</button>
            </span>
        </div>
    )
  }
}
