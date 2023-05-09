import React, { Component } from 'react'
import { MapContainer, TileLayer, useMap, Marker, Popup } from 'react-leaflet'
import Car from './car.tsx'

interface IProps {
    map : any,
    cars: []
  }

export default class Map_container extends Component<IProps> {
  render() {
    return (
        <MapContainer bounds={this.props.map } scrollWheelZoom={true} > 
        <TileLayer
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        attribution='&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
        />
        {this.props.cars.map(oneCar=> {
          return <Car car={oneCar}></Car>
        })}
        {/* <Car car={this.props.cars} statistics={"statistics"}></Car> */}
      </MapContainer>
    )
  }
}
