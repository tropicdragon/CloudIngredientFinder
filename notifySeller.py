import boto3
import json
import decimal
from boto3.dynamodb.conditions import Key, Attr
from botocore.exceptions import ClientError

# Helper class to convert a DynamoDB item to JSON.
class DecimalEncoder(json.JSONEncoder):
    def default(self, o):
        if isinstance(o, decimal.Decimal):
            if o % 1 > 0:
                return float(o)
            else:
                return int(o)
        return super(DecimalEncoder, self).default(o)
        
def lambda_handler(event, context):
    # TODO implement
    #lambda_client = boto3.client('lambda', region_name='us-east-1')
    #print(event['sellerID'])
    print("Hello")
    print (event)
    #print (event['itemname'])
    lowItems = event['loItems']
    print(lowItems)
    
    if (len(lowItems) == 0):
        return
    
    sellerID=event['sellerID']
    dynamodb = boto3.resource("dynamodb", region_name='us-east-1', endpoint_url="https://dynamodb.us-east-1.amazonaws.com")

    table = dynamodb.Table('seller')
    sns=boto3.client('sns')



    try:
        response = table.get_item(
            Key={
                'sellerID':sellerID
            }
        )
        print(response)
    except ClientError as e:
        print(e.response['Error']['Message'])
    else:
        item = response['Item']
        
        print("GetItem succeeded:")
        fitem=json.dumps(item, indent=4, cls=DecimalEncoder)
        fitem1 = json.loads(fitem)
        phonenum = fitem1["sellercontact"]
        print(phonenum)
        final_list = "-->"
        for i in lowItems:
            final_list += i["itemname"] + " "
        print(final_list)
        msg = "You are running out of stock on following items"+ final_list
        print(msg)
        respo=sns.publish(PhoneNumber = phonenum, Message=msg, Subject="Low Stock")
        #respo=sns.publish(PhoneNumber = phonenum, Message="You are running out of stock"+string(lowItems), Subject="Low Stock")
        #respo=sns.publish(PhoneNumber = phonenum, Message="Hey!, whatsup", Subject="Low Stock")

        print("message sent")

    
    return 'Hello from Lambda'

