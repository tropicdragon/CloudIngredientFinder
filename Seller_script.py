
# coding: utf-8

# In[ ]:


import paho.mqtt.client as mqtt
import json
import time
import random


# In[ ]:


# Define Variables
MQTT_HOST = "ec2-54-197-24-57.compute-1.amazonaws.com"
MQTT_PORT = 1883
MQTT_KEEPALIVE_INTERVAL = 60
MQTT_TOPIC = "localgateway_to_awsiot"


# In[ ]:



latitude_range = 17
longitude_range = 45
number_of_items = 10
item_list = ["apple", "orange", "strawberry", "garlic", "egg", "butter", "tomato", "cheese", "bread", "sugar"]
price_list = [1,1.5, 0.75, 0.5, 3, 5, 1, 10, 3, 2]
number_of_sellers = 10
seller_names = ["traderjoes", "publix", "bigbizaar", "wholefoods", "seven11", "traderjoes", "publix", "bigbizaar", "wholefoods", "seven11"]
seller_contacts = ["1-305-510-6936", "1-646-683-9379","1-646-683-9379","1-646-683-9379","1-646-683-9379", "1-646-683-9379", "1-646-683-9379","1-646-683-9379","1-646-683-9379","1-646-683-9379"]
max_quantity = 500

MQTT_MSG = None


# In[ ]:


def create_seller():
    list_of_sellers = []
    for i in range(number_of_sellers):
        lat = random.randint(0,latitude_range) + 32
        long = random.randint(0,longitude_range) - 121
        quantity = random.randint(0,max_quantity)
        msg_to_send ={
            "sellerID" : i ,
            "sellername" : seller_names[i],
           # "notified" : ""
            "usertype" : "seller",
            "sellercontact" : seller_contacts[i],
            "location" :
            {
                "lat" : lat,
                "long" : long
            },

            "Items" : []
        }
        for j in range(number_of_items):
            random_prob = random.randint(0,10)
            if(random_prob < 4):
                msg_to_send["Items"].append(
                {
                "itemID" : j,
                "itemname" : item_list[j],
                "price" : price_list[j],
                "quantity" : random.randint(0,max_quantity)
                }
                )
            else:
                print("Item: ",j)
                msg_to_send["Items"].append(
                {
                "itemID" : j,
                "itemname" : item_list[j],
                "price" : price_list[j],
                "quantity" : -1
                }
                )
            
        msg =json.dumps(msg_to_send)
        list_of_sellers.append(msg)
    return(list_of_sellers)



# In[ ]:



list_of_sellers = create_seller()


# In[ ]:


list_of_sellers


# In[ ]:


def update_sellers(sellers):
    buying_chance = 8
    new_list_of_sellers = []
    for i,seller in enumerate(sellers):
        msg = json.loads(seller)
        name = msg['sellername']
        for j in range(number_of_items):
            n = random.randint(0,10)
            if(msg['Items'][j]['quantity'] == -1):
                continue
            if(n <= buying_chance):
                msg['Items'][j]['quantity'] -= n * 5
                msg['Items'][j]['quantity'] = max(msg['Items'][j]['quantity'], 0)
            else:
                msg['Items'][j]['quantity'] += n * 5
                msg['Items'][j]['quantity'] = min(msg['Items'][j]['quantity'], max_quantity)
        
      #  print(name)
        print(msg)
        new_msg = json.dumps(msg)
        new_list_of_sellers.append(new_msg)
    return(new_list_of_sellers)
        


# In[ ]:


#list_of_sellers = update_sellers(list_of_sellers)


# In[ ]:



def on_publish(client, userdata, mid):
    print ("Message Published...")

def on_connect(client, userdata, flags, rc):
    #client.subscribe(MQTT_TOPIC)
    client.publish(MQTT_TOPIC, MQTT_MSG)
    client.disconnect()

def on_message(client, userdata, msg):
    print(msg.topic)
    print(msg.payload) # <- do you mean this payload = {...} ?
   # payload = json.loads(msg.payload) # you can use json.loads to convert string to json
    #print(payload['sepalWidth']) # then you can check the value
    client.disconnect() # Got message then disconnect

# Initiate MQTT Client
mqttc = mqtt.Client()

# Register publish callback function
mqttc.on_publish = on_publish
mqttc.on_connect = on_connect
mqttc.on_message = on_message




# In[ ]:



for i in range(1000):
    for j in range(number_of_sellers):
        print(j)
    # Connect with MQTT Broker
        MQTT_MSG = list_of_sellers[j]
        time.sleep(2)
        mqttc.connect(MQTT_HOST, MQTT_PORT, MQTT_KEEPALIVE_INTERVAL)
    # Loop forever
        mqttc.loop_forever()
    time.sleep(10)
    list_of_sellers = update_sellers(list_of_sellers)
    time.sleep(10)

