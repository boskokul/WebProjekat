const LogingIn = {template: '<logingPage></logingPage>'}
const RegisterPage = {template: '<registerPage></registerPage>'}
const ProfilePage = {template: '<profilePage></profilePage>'}
const EditProfile = {template: '<editProfile></editProfile>'}
const ChangePassword = {template: '<changePassword></changePassword>'}
const RentACar = {template: '<rentACar></rentACar>'}
const RentACarTemplate = {template: '<rentACarTemplate></rentACarTemplate>'}
const RentACarMenu = {template: '<rentACarMenu></rentACarMenu>'}

const router = new VueRouter({
	mode: 'hash',
	routes: [
		{path: '/', name: 'home', component: LogingIn},
		{path: '/register/', component: RegisterPage},
		{path: '/user/', component: ProfilePage},
		{path: '/:user/edit/', component: EditProfile},
		{path: '/:user/changepassword/', component: ChangePassword},
		{path: '/rentacar/', component: RentACar},
		{path: '/rentacartemplate/', component: RentACarTemplate},
		{path: '/rentacarmenu/', component: RentACarMenu}
	]
})

var App = new Vue({
	router,
	el: '#contentDiv'
})