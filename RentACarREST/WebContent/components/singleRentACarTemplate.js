Vue.component("singleRentACarTemplate",{
	data:function(){
		return{
			rentACar: null,
			id: -1
		}
	},
	template: `
	<div>
	<div style="float:left; margin:10px">
		<img v-bind:src="rentACar.logoImg" style="width:100px; height:100px" />
	</div>
		<div style="float:left; margin:10px; font-size:20px">
			<h1 style="font-size:40px">{{rentACar.name}}</h1>
			<div style="float:left; margin:10px">
				Location:
			</div>
			<div style="float:left; margin:10px; margin-left:50px">
		        	{{rentACar.location.streetNumber}} <br>
		        	{{rentACar.location.placeZipCode}}<br> 
		        	{{rentACar.location.longitude}}, {{rentACar.location.latitude}}
	        </div>
	        <div>
	        <div style="float:left; margin:10px">
	        	Working time:
	        </div>
	        <div style="float: left; margin:10px">
	        	{{rentACar.startHour}}:{{rentACar.startMinute}} - {{rentACar.endHour}}:{{rentACar.endMinute}}
	        	<br>
	        	{{rentACar.status}}
			</div>
			</div>
			<div>
	        <div style="float:left; margin:10px">
	        	Rating:
	        </div>
	        <div style="float: left; margin:10px; margin-left:65px">
	        	{{rentACar.grade}}/10
			</div>
			</div>
			
		</div>
		<br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br>
			<div style="clear:both">
			<div style="margin: 50px; margin-left: 120px">
				<h2 style="text-align:left"> Comments </h2>
				<div style="width:600px; height:200px; margin:20px; border:1px solid">
				to be done
				</div>
			</div>
			</div>
			<div style="margin: 50px; margin-left: 120px">
				<h2 style="text-align:left"> Vehicles </h2>
				<div v-for="v in rentACar.vehicles" style="border:1px solid black; font-size:21px; padding: 10px; width: 70%; margin: 0% 12% 1% 12%; background-color: #FBD603">
	    	<div>
				<div class="row">
					<div class="column">
						<div class="image">
							<img v-bind:src="v.photo" style="width:140px; height:100px" />
						</div>
						<div>
							<label>Model:</label>
							<b><label>{{v.model}}</label></b>
						</div>
					</div>
					<div class="column">
						<label>Brand:</label>
						<b><label>{{v.brand}}</label></b><br>
						<label>Price:</label>
						<b><label>{{v.price}}</label></b><br>
						<label>Type:</label>
						<b><label>{{v.type}}</label></b><br>
						<label>Fuel type:</label>
						<b><label>{{v.fuelType}}</label></b><br>
					</div>
					<div class="column">
						<label>Consumption:</label>
						<b><label>{{v.consumption}}</label></b><br>
						<label>Number of doors:</label>
						<b><label>{{v.doors}}</label></b><br>
						<label>Max people:</label>
						<b><label>{{v.maxPeople}}</label></b><br>
						<label>Desription:</label>
						<b><label>{{v.description}}</label></b><br><br>
					</div>
				</div>
			</div>
	    </div>
		</div>
	</div>
	`,
	
	mounted: function() {
    	this.id = this.$route.params.id
    	console.log(this.id)
    	axios.get('rest/rentacar/'+this.id)
    	.then(response => this.rentACar = response.data)
    },
	methods:{
	
	}
})