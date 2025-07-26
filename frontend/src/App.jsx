import './App.css'
import {Route, Routes, useNavigate } from 'react-router-dom'
import {setNavigate} from "./navigateHelper";
import React, { useEffect } from "react";
import '@ant-design/v5-patch-for-react-19';
import HomePage from './components/HomePage.jsx'
import BlogEdit from "./components/BlogEdit.jsx";
import Login from './components/Login.jsx'
import Login2 from './components/Login2.jsx'
import UserProfile from './components/UserProfile.jsx'
import BlogDetail from "./components/BlogDetail.jsx";
import ShopList from "./components/ShopList.jsx";
import ShopDetail from "./components/ShopDetail.jsx";

function App() {
    const navigate = useNavigate();

    useEffect(() => {
        setNavigate(navigate);
    }, [navigate]);
    return (

            <Routes>
                <Route path='/' element={<HomePage/>}> </Route>
                <Route path='/login' element={<Login/>}></Route>
                <Route path='/login2' element={<Login2/>}></Route>
                <Route path='/blog-edit' element={<BlogEdit/>}></Route>
                <Route path='/my-info' element={<UserProfile/>}></Route>
                <Route path='/blog-detail' element={<BlogDetail/>}></Route>
                <Route path='/shop-list' element={<ShopList/>}></Route>
                <Route path='/shop-detail' element={<ShopDetail/>}></Route>

            </Routes>
    )
}

export default App

// import React from 'react';
//
// function App() {
//     return (
//         <div className="App">
//             <h1>Welcome to React</h1>
//         </div>
//     );
// }
//
// export default App;