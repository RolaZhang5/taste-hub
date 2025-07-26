import React, { useState } from 'react';
import { Input, Button, message, Radio } from 'antd';
import axios from "/public/js/common.js";
import { Link } from 'react-router-dom';
import { navigateTo } from "../navigateHelper.js";
import styles from '/public/css/info.module.css';

axios.defaults.baseURL = '/api';

const Login = () => {
    const [form, setForm] = useState({ phone: '', code: '' });
    const [radio, setRadio] = useState(false);
    const [disabled, setDisabled] = useState(false);
    const [codeBtnMsg, setCodeBtnMsg] = useState('Send Code');

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setForm({ ...form, [name]: value });
    };

    const login = () => {
        if (!radio) {
            message.error('Please confirm you have read the User Agreement first!');
            return;
        }
        if (!form.phone || !form.code) {
            message.error('Phone number and verification code cannot be empty!');
            return;
        }
        console.log("Logging in");
        axios
            .post('/user/login', form)
            .then(({ data }) => {
                if (data) {
                    sessionStorage.setItem('token', data);
                }
                navigateTo('/my-info');
            })
            .catch((err) => message.error(err));
    };

    const goBack = () => {
        navigateTo("/");
    };

    const sendCode = () => {
        if (!form.phone) {
            message.error('Phone number cannot be empty');
            return;
        }
        axios
            .post(`/user/code?phone=${form.phone}`)
            .then(() => {
                // code sent
            })
            .catch((err) => {
                console.log(err);
                message.error(err);
            });
        setDisabled(true);
        let i = 60;
        setCodeBtnMsg(`${i--} seconds before resend`);
        const taskId = setInterval(() => {
            setCodeBtnMsg(`${i--} seconds before resend`);
        }, 1000);
        setTimeout(() => {
            setDisabled(false);
            clearInterval(taskId);
            setCodeBtnMsg('Send Code');
        }, 59000);
    };

    return (
        <div id="app">
            <div className="login-container">
                <div className="header">
                    <div className="header-back-btn" onClick={goBack}>
                        <i className="el-icon-arrow-left"></i>
                    </div>
                    <div className="header-title" style={{ width: '90%' }}>
                        Quick Login via Phone Number
                    </div>
                </div>
                <div className="content">
                    <div className="login-form">
                        <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                            <Input
                                style={{ width: '60%' }}
                                placeholder="Enter your phone number"
                                name="phone"
                                value={form.phone}
                                onChange={handleInputChange}
                            />
                            <Button
                                style={{ width: '38%', backgroundColor: '#67C23A' }}
                                onClick={sendCode}
                                type="primary"
                                disabled={disabled}
                            >
                                {codeBtnMsg}
                            </Button>
                        </div>
                        <div style={{ height: '5px' }}></div>
                        <Input
                            placeholder="Enter the verification code"
                            name="code"
                            value={form.code}
                            onChange={handleInputChange}
                        />
                        <div style={{ textAlign: 'center', color: '#8c939d', margin: '5px 0' }}>
                            Unregistered phone numbers will automatically create an account upon verification
                        </div>
                        <Button
                            onClick={login}
                            style={{ width: '100%', backgroundColor: '#f63', color: '#fff' }}
                        >
                            Login
                        </Button>
                        <div style={{ textAlign: 'right', color: '#333333', margin: '5px 0' }}>
                            <Link to="/login2">Login with Password</Link>
                        </div>
                    </div>
                    <div className="login-radio">
                        <div>
                            <Radio
                                checked={radio}
                                onChange={(e) => setRadio(e.target.checked)}
                            />
                        </div>
                        <div>
                            I have read and agree to the
                            <a href="javascript:void(0)"> TasteHub User Service Agreement</a> and
                            <a href="javascript:void(0)"> Privacy Policy</a>, including clauses highlighted in bold such as exemption or limitation of liability and jurisdiction.
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Login;
