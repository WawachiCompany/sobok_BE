const axios = require('axios');

/** 응답에서 토큰·쿠키를 안전하게 꺼내고, 환경변수+요청 헤더에 심어준다 */
function applyAuth(req, res) {
  // 헤더 또는 바디에서 토큰 추출 (프로젝트 응답 스펙에 맞춰 조정)
  const headerAuth = res?.headers?.authorization || res?.headers?.Authorization;
  const body = res?.data || {};
  const bodyToken = body.accessToken || body.token || body.jwt || body.id_token;

  const bearer = headerAuth
      ? headerAuth.startsWith('Bearer ') ? headerAuth : `Bearer ${headerAuth}`
      : (bodyToken ? (bodyToken.startsWith('Bearer ') ? bodyToken
          : `Bearer ${bodyToken}`) : null);

  // 쿠키(있으면)
  const setCookie = res?.headers?.['set-cookie'];
  const cookieStr = Array.isArray(setCookie) ? setCookie.join('; ') : setCookie;

  if (bearer) {
    // 다른 요청에서도 재사용하고 싶다면 env로 저장
    bru.setEnvVar('accessToken', bearer);
    req.setHeader('Authorization', bearer);
  }
  if (cookieStr) {
    bru.setEnvVar('authCookie', cookieStr);
    req.setHeader('Cookie', cookieStr);
  }
}

async function login(req, creds) {
  try {
    const baseUrl = bru.getEnvVar('base'); // ← 너희 환경변수 이름이 'base'인 걸로 확인함
    const res = await axios.post(`${baseUrl}/user/login/jwt`, creds,
        {timeout: 5000});
    applyAuth(req, res);
    return res;
  } catch (error) {
    if (axios.isAxiosError(error)) {
      error.message += ' (in pre-script login)';
    }
    throw error;
  }
}

/** 편의 래퍼들 */

async function loginByCase(req, caseId) {
  const map = {
    "postman": {username: "postman", password: "1q2w3e4r"},
    "aszini": {username: "aszini", password: "password1234!"},
    "chulsoo": {"username": "chulsoo", "password": "password"},
    "younghee": {username: "younghee", password: "password"},
    "minsoo": {username: "minsoo", password: "password"},

  };
  const creds = map[caseId];
  if (!creds) {
    throw new Error(`Unknown caseId: ${caseId}`);
  }
  return login(req, creds);
}

module.exports = {login, loginByCase};