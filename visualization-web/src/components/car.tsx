import React, { Component } from 'react'
import { Marker, Popup } from 'react-leaflet'

interface IProps {
    car: any
  }

export default class Map_container extends Component<IProps> {
  render() {
    return (
        <Marker position={this.props.car.position}>
            <Popup>
              <ul>
                <li>ID: {this.props.car.carId}</li>
                <li>SPEED: {this.props.car.speed}</li>
                <li>ACCELERATION: {this.props.car.acceleration}</li>
                <li>POSITION: {this.props.car.position}</li>
              </ul>
            </Popup>
        </Marker>
    )
  }
}