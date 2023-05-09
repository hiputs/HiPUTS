import React, { Component } from 'react'
import Map_container from './map_container.tsx'
import Navbar from './navbar.tsx'

interface IProps {
    map : any,
    cars: any
  } 
  
export default class Visualization extends Component<IProps> {

  render() {
    return (
        <>
        <Navbar></Navbar>
      <Map_container  map={this.props.map} cars={this.props.cars}></Map_container>
      </>
    )
  }
}
