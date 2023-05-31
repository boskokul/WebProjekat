Vue.component("profilePage",{
	data:function(){
		return{
			id: null,
			customer: {id: null, username: null, password: null, firstName: null, 
			lastName: null, gender: null, role: null, dateOfBirth: null}
		}
	},
	template: `
	<div>
		<div style="background-color:powderblue; font-size:20px">
			<a href="" v-on:click="edit">Edit profile</a>
			<a href="" v-on:click="edit">Rent a car shops</a>
		</div>
	    <div style="border:1px solid black; padding-left: 5px;">
			<h1 style="color:red;">Profile page</h1>
	        <h2>{{customer.firstName}} {{customer.lastName}}</h2>
	    
	    <div style="padding-left: 5px; font-size:20px">
	        <label>Username:</label>
	        <label>{{customer.username}}</label>
	    </div>
	    <div style="padding-left: 5px;">
	        <label>Gender:</label>
	        <label>{{customer.gender}}</label><br>
	        <label>Date of birth:</label>
	        <label>{{customer.dateOfBirth}}</label>
	    </div>
	    </div>
	</div>
	`,
	mounted: function() {
    	this.id = this.$route.params.id;
    	axios.get("rest/customers/"+this.id)
			.then(response =>	
				this.customer = response.data)
		setTimeout(() => {
        	this.formatDate()
      	}, 1000)
    },
	methods:{
		edit: function(){
			event.preventDefault()
			router.push(`/customer/edit/${this.id}`)
		},
		formatDate: function(){
			let date = new Date(this.customer.dateOfBirth)
			let day = date.getDate()
			let month = date.getMonth()+1
			let year = date.getFullYear()
			let zeroAtDay = ''
			let zeroAtMonth = ''
			if(day<10){
				zeroAtDay = '0'
			}
			if(month<10){
				zeroAtMonth = '0'
			}
			this.customer.dateOfBirth = year+'-'+zeroAtMonth+month+'-'+zeroAtDay+day
			console.log(this.customer.dateOfBirth)	
		}
	}
})