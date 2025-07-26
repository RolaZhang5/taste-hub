import React, { useState, useEffect } from 'react';
import axios from 'axios';
axios.defaults.baseURL = '/api'; // Set global request prefix
// import { Message } from 'element-react'; // If using Element React
import { useHistory } from 'react-router-dom'; // For navigation

const UserProfileEdit = () => {
  const [user, setUser] = useState({});
  const [info, setInfo] = useState({});
  const history = useHistory();

  useEffect(() => {
    checkLogin();
  }, []);

  const checkLogin = () => {
    axios.get('/user/me')
        .then(({ data }) => {
          setUser(data);
          const storedInfo = sessionStorage.getItem('userInfo');
          setInfo(storedInfo ? JSON.parse(storedInfo) : {});
        })
        .catch(err => {
          // Message.error(err.message); // If using Element React
          alert(err.message); // Alternative
          setTimeout(() => {
            history.push('/login');
          }, 1000);
        });
  };

  const goBack = () => {
    history.goBack();
  };

  return (
      <div id="app">
        <div className="header">
          <div className="header-back-btn" onClick={goBack}>
            <i className="el-icon-arrow-left"></i>
          </div>
          <div className="header-title">Edit Profile&nbsp;&nbsp;&nbsp;</div>
        </div>
        <div className="edit-container">
          <div className="info-box">
            <div className="info-item">
              <div className="info-label">Avatar</div>
              <div className="info-btn">
                <img
                    width="35"
                    src={user.icon || '/imgs/icons/default-icon.png'}
                    alt=""
                />
                <div><i className="el-icon-arrow-right"></i></div>
              </div>
            </div>
            <div className="divider"></div>
            <div className="info-item">
              <div className="info-label">Nickname</div>
              <div className="info-btn">
                <div>{user.nickName}</div>
                <div><i className="el-icon-arrow-right"></i></div>
              </div>
            </div>
            <div className="divider"></div>
            <div className="info-item">
              <div className="info-label">Introduction</div>
              <div className="info-btn">
                <div style={{ overflow: 'hidden', width: '150px', textAlign: 'right' }}>
                  {info.introduce || 'Introduce yourself'}
                </div>
                <div><i className="el-icon-arrow-right"></i></div>
              </div>
            </div>
          </div>
          <div className="info-box">
            <div className="info-item">
              <div className="info-label">Gender</div>
              <div className="info-btn">
                <div>{info.gender || 'Select'}</div>
                <div><i className="el-icon-arrow-right"></i></div>
              </div>
            </div>
            <div className="divider"></div>
            <div className="info-item">
              <div className="info-label">City</div>
              <div className="info-btn">
                <div>{info.city || 'Select'}</div>
                <div><i className="el-icon-arrow-right"></i></div>
              </div>
            </div>
            <div className="divider"></div>
            <div className="info-item">
              <div className="info-label">Birthday</div>
              <div className="info-btn">
                <div>{info.birthday || 'Add'}</div>
                <div><i className="el-icon-arrow-right"></i></div>
              </div>
            </div>
          </div>
          <div className="info-box">
            <div className="info-item">
              <div className="info-label">My Points</div>
              <div className="info-btn">
                <div>View Points</div>
                <div><i className="el-icon-arrow-right"></i></div>
              </div>
            </div>
            <div className="divider"></div>
            <div className="info-item">
              <div className="info-label">Membership Level</div>
              <div className="info-btn">
                <div><a href="javascript:void(0)">Become a VIP for exclusive privileges</a></div>
                <div><i className="el-icon-arrow-right"></i></div>
              </div>
            </div>
          </div>
        </div>
        {/* <FootBar activeBtn={4} /> */}
      </div>
  );
};

export default UserProfileEdit;
