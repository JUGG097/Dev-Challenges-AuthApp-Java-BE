# Authentication App Java/SpringBoot Backend Project (The backend for the Auth App website deployed [here](https://authapp-adeoluwa.netlify.app/))

This project was developed using `Java` v "17" and `Spring Boot` v "2.7.3".

Deployed on a `Digital Oceans` Droplet using `Github Actions` for CI/CD.

The Auth App Website was deployed with `Netlify` link [here](https://authapp-adeoluwa.netlify.app/).

Figma design was provided by [devChallenges.io](https://devchallenges.io/).

You can clone project and customise at your end.

## API Documentation
*http://127.0.0.1:8000/check Endpoint (server health check)*

- METHOD: 'GET'

- SUCCESS RESPONSE (200): {'success': true}

- ERROR RESPONSE (4**, 5**): {'success': false, 'message': '***********'}


*http://127.0.0.1:8000/api/v1/auth/signup Endpoint*

- METHOD: 'POST'

- REQUEST BODY: {
  "email": "JohnDoe@gmail.com",
  "password": "ty12243fghhh",
  "provider": "LOCAL",
  }

- SUCCESS RESPONSE (200): {
  'success': true,
  'authToken': '**********',
  'refreshToken': '**********',
  'data': {}
  }

- ERROR RESPONSE (4**, 5**): {'***********'}

*http://127.0.0.1:8000/api/v1/auth/login Endpoint*

- METHOD: 'POST'

- REQUEST BODY: {
  "email": "JohnDoe@gmail.com",
  "password": "ty12243fghhh",
  "provider": "LOCAL",
  }

- SUCCESS RESPONSE (200): {
  'success': true,
  'authToken': '**********',
  'refreshToken': '**********',
  'data': {}
  }

- ERROR RESPONSE (4**, 5**): {'***********'}


*http://127.0.0.1:8000/api/v1/auth/oauthLogin Endpoint*

- METHOD: 'POST'

- REQUEST BODY: {
  "email": "JohnDoe@gmail.com",
  "password": "ty12243fghhh",
  "provider": "oAuth Provider",
  }

- SUCCESS RESPONSE (200): {
  'success': true,
  'authToken': '**********',
  'refreshToken': '**********',
  'data': {}
  }

- ERROR RESPONSE (4**, 5**): {'***********'}

*http://127.0.0.1:8000/api/v1/auth/githubOauth Endpoint*

- METHOD: 'GET'

- REQUEST PARAMS: {
  'code': 'sdsssas',
  'mode': 'login'
  }

- SUCCESS RESPONSE (200): {
  'success': true,
  'authToken': '**********',
  'refreshToken': '**********',
  'data': {}
  }

- ERROR RESPONSE (4**, 5**): {'***********'}

*http://127.0.0.1:8000/api/v1/auth/refreshToken Endpoint*

- METHOD: 'POST'

- REQUEST BODY: {
  "refreshToken": "awerra233",
  }

- SUCCESS RESPONSE (200): {
  'success': true,
  'authToken': '**********',
  'refreshToken': '**********',
  'data': {}
  }

- ERROR RESPONSE (4**, 5**): {'***********'}

*http://127.0.0.1:8000/api/v1/user/profile Endpoint (Protected)*

- METHOD: 'GET'

- AUTHORIZATION: 'Bearer <access_token>'

- SUCCESS RESPONSE (200): {'success': true, 'data': {}}

- ERROR RESPONSE (4**, 5**): {'success': false, 'message': '***********'}

*http://127.0.0.1:8000/api/v1/user/editProfile Endpoint (Protected)*

- METHOD: 'PUT'

- AUTHORIZATION: 'Bearer <access_token>'

- REQUEST BODY: {
  "name": "JohnDoe@gmail.com",
  "bio": "ty12243fghhh",
  "image": "*****",
  "phoneNumber": 23244242,
  }

- SUCCESS RESPONSE (200): {'success': true,  'data': {}}

- ERROR RESPONSE (4**, 5**): {'success': false, 'message': '***********'}
