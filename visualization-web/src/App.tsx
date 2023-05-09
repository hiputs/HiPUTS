import './App.css';
import React from "react";
import { Component } from 'react'
import { ClipLoader } from 'react-spinners';
import Visualization from './components/visualization.tsx';
import Socket from './Utils/Comunication.ts';



interface IProps {
}
export interface IAppState {
  map: any,
  cars: any,
  statistics: any,
  loading: boolean
}

class App extends Component<IProps, IAppState> {
  constructor(props: IProps){
    super(props);
    this.state = {map : null, cars: [], statistics : null, loading : true};
    Socket.initialize(`ws://${window.location.hostname}:8080/visualization`);
    Socket.addMessageCallback((msg: any ) => {
      if(msg.type =='map'){
        this.setState(prevState=> ({...prevState, map: msg.data,loading: false}));
      }
      else if(msg.type == 'cars'){
        this.setState(prevState=> ({...prevState, cars: msg.data}));
      }
      console.log("Get message:", msg);
    
    })
    
  }
  render() {
    console.log(" Configuration: ", this.state);
    const load_style={ position: "fixed", top: "50%", left: "50%", transform: "translate(-50%, -50%)" };
    return <>
    <div style={load_style}>
      {this.state.loading?
      <ClipLoader loading = {this.state.loading} speedMultiplier={0.5}></ClipLoader>
       : <Visualization map={this.state.map} cars={this.state.cars}></Visualization> }
    </div>
    </>
  }
}
export default App;
