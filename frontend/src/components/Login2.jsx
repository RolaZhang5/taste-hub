import React, { useState } from 'react';
import axios from 'axios';
axios.defaults.baseURL = '/api'; // Set global request prefix
import '/public/css/login.css';
import '/public/css/element.css';
import {navigateTo} from "../navigateHelper.js";

function PasswordLogin() {
    const [form, setForm] = useState({ phone: '', password: '' });
    const [radio, setRadio] = useState(false);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setForm((prevForm) => ({
            ...prevForm,
            [name]: value,
        }));
    };

    const handleRadioChange = () => {
        setRadio(!radio);
    };

    const login = () => {
        if (!radio) {
            alert('Please confirm you have read the User Agreement first!');
            return;
        }
        if (!form.phone || !form.password) {
            alert('Phone number and password cannot be empty!');
            return;
        }
        axios
            .post('/user/login', form)
            .then(({ data }) => {
                if (data) {
                    sessionStorage.setItem('token', data);
                    navigateTo('/my-info');
                }
            })
            .catch((err) => {
                alert('Login failed, please check your information.');
                console.error(err);
            });
    };

    const goBack = () => {
        window.history.back();
    };

    return (
        <div className="login-container">
            <div className="header">
                <div className="header-back-btn" onClick={goBack}>
                    <i className="el-icon-arrow-left"></i>
                </div>
                <div className="header-title">Password Login&nbsp;&nbsp;&nbsp;</div>
            </div>
            <div className="content">
                <div className="login-form">
                    <input
                        type="text"
                        name="phone"
                        placeholder="Please enter phone number"
                        value={form.phone}
                        onChange={handleChange}
                        className="el-input__inner"
                    />
                    <div style={{ height: '5px' }}></div>
                    <input
                        type="password"
                        name="password"
                        placeholder="Please enter password"
                        value={form.password}
                        onChange={handleChange}
                        className="el-input__inner"
                    />
                    <div style={{ textAlign: 'center', color: '#8c939d', margin: '5px 0' }}>
                        <a href="javascript:void(0)">Forgot password</a>
                    </div>
                    <button
                        onClick={login}
                        style={{ width: '100%', backgroundColor: '#f63', color: '#fff' }}
                        className="el-button"
                    >
                        Login
                    </button>
                    <div style={{ textAlign: 'right', color: '#333333', margin: '5px 0' }}>
                        <a href="/login">Login with verification code</a>
                    </div>
                </div>
                <div className="login-radio">
                    <div>
                        <input
                            type="checkbox"
                            name="readed"
                            checked={radio}
                            onChange={handleRadioChange}
                        />
                        <label htmlFor="readed"></label>
                    </div>
                    <div>
                        I have read and agree to the
                        <a href="javascript:void(0)"> TasteHub User Service Agreement</a> and
                        <a href="javascript:void(0)"> Privacy Policy</a>, including clauses highlighted in bold such as exemption or limitation of liability and jurisdiction.
                    </div>
                </div>
            </div>
        </div>
    );
}

export default PasswordLogin;
