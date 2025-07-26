import React, { useState, useEffect } from 'react';
import axios from 'axios';
axios.defaults.baseURL = '/api'; // Set global request prefix
import '/publi/css/element.css';
import '/publi/css/main.css';
import '/publi/css/info.css';
import { message } from "antd";

function OtherInfo() {
  const [user, setUser] = useState({});
  const [loginUser, setLoginUser] = useState({});
  const [info, setInfo] = useState({});
  const [blogs, setBlogs] = useState([]);
  const [followed, setFollowed] = useState(false);
  const [commonFollows, setCommonFollows] = useState([]);
  const [activeName, setActiveName] = useState('1');

  useEffect(() => {
    const id = new URLSearchParams(window.location.search).get('id');
    axios.get(`/user/${id}`)
        .then(({ data }) => {
          setUser(data);
          queryUserInfo(data.id);
          queryBlogs(data.id);
          isFollowed(data.id);
        })
        .catch(console.error);

    axios.get('/user/me')
        .then(({ data }) => setLoginUser(data))
        .catch(console.error);
  }, []);

  const queryUserInfo = (id) => {
    axios.get(`/user/info/${id}`)
        .then(({ data }) => {
          if (data) {
            setInfo(data);
            sessionStorage.setItem('userInfo', JSON.stringify(data));
          }
        })
        .catch(console.error);
  };

  const queryBlogs = (id) => {
    axios.get('/blog/of/user', { params: { id, current: 1 } })
        .then(({ data }) => setBlogs(data))
        .catch(console.error);
  };

  const isFollowed = (id) => {
    axios.get(`/follow/or/not/${id}`)
        .then(({ data }) => setFollowed(data))
        .catch(console.error);
  };

  const queryCommonFollow = () => {
    axios.get(`/follow/common/${user.id}`)
        .then(({ data }) => setCommonFollows(data))
        .catch(console.error);
  };

  const handleFollow = () => {
    axios.put(`/follow/${user.id}/${!followed}`)
        .then(() => {
          message.info(followed ? 'Unfollowed' : 'Followed');
          setFollowed(!followed);
        })
        .catch(console.error);
  };

  const handleTabClick = (name) => {
    setActiveName(name);
    if (name === '2') {
      queryCommonFollow();
    }
  };

  const goBack = () => {
    window.history.back();
  };

  const toOtherInfo = (id) => {
    window.location.href = `/other-info.html?id=${id}`;
  };

  return (
      <div id="app">
        <div className="header">
          <div className="header-back-btn" onClick={goBack}>
            <i className="el-icon-arrow-left"></i>
          </div>
          <div className="header-title">&nbsp;&nbsp;&nbsp;</div>
        </div>
        <div className="basic">
          <div className="basic-icon">
            <img src={user.icon || '/imgs/icons/default-icon.png'} alt="" />
          </div>
          <div className="basic-info">
            <div className="name">{user.nickName}</div>
            <span>Hangzhou</span>
          </div>
          <div className="logout-btn" onClick={handleFollow} style={{ textAlign: 'center' }}>
            {followed ? 'Unfollow' : 'Follow'}
          </div>
        </div>
        <div className="introduce">
          <span>{info.introduce || 'This person is lazy and left nothing behind.'}</span>
        </div>
        <div className="content">
          <div className="el-tabs">
            <div className="el-tabs__header">
              <div
                  className={`el-tabs__item ${activeName === '1' ? 'is-active' : ''}`}
                  onClick={() => handleTabClick('1')}
              >
                Notes
              </div>
              <div
                  className={`el-tabs__item ${activeName === '2' ? 'is-active' : ''}`}
                  onClick={() => handleTabClick('2')}
              >
                Mutual Follows
              </div>
            </div>
            <div className="el-tabs__content">
              {activeName === '1' && (
                  <div className="el-tab-pane">
                    {blogs.map((b) => (
                        <div key={b.id} className="blog-item">
                          <div className="blog-img">
                            <img src={b.images.split(',')[0]} alt="" />
                          </div>
                          <div className="blog-info">
                            <div className="blog-title" dangerouslySetInnerHTML={{ __html: b.title }}></div>
                            <div className="blog-liked">
                              <img src="/imgs/thumbup.png" alt="" /> {b.liked}
                            </div>
                            <div className="blog-comments">
                              <i className="el-icon-chat-dot-round"></i> {b.comments}
                            </div>
                          </div>
                        </div>
                    ))}
                  </div>
              )}
              {activeName === '2' && (
                  <div className="el-tab-pane">
                    <div>You both follow:</div>
                    {commonFollows.map((u) => (
                        <div key={u.id} className="follow-info">
                          <div className="follow-info-icon" onClick={() => toOtherInfo(u.id)}>
                            <img src={u.icon || '/imgs/icons/default-icon.png'} alt="" />
                          </div>
                          <div className="follow-info-name">
                            <div className="name">{u.nickName}</div>
                          </div>
                          <div className="follow-info-btn" onClick={() => toOtherInfo(u.id)}>
                            Visit homepage
                          </div>
                        </div>
                    ))}
                  </div>
              )}
            </div>
          </div>
        </div>
        {/* Add foot-bar component here if needed */}
      </div>
  );
}

export default OtherInfo;
