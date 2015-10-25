from clarifai_basic import ClarifaiCustomModel
import os
import urllib2, socket

# instantiate clarifai client
clarifai = ClarifaiCustomModel()
p=os.getcwd()
p=p.replace('\\','/')

#XXXXXXXXXXXXXXXXXXX CAR XXXXXXXXXXXXXXXXXXXXXXXXXXXXX
POSITIVES = []
pos=p+"/images/cars.txt"
with open(pos) as f:
    POSITIVES = [x.strip('\n') for x in f.readlines()]

NEGATIVES = []
neg=p+"/images/bikes.txt"
with open(neg) as f:
    NEGATIVES.extend([x.strip('\n') for x in f.readlines()])
    
neg=p+"/images/buses.txt"
with open(neg) as f:
    NEGATIVES.extend([x.strip('\n') for x in f.readlines()])
    
neg=p+"/images/motorbikes.txt"
with open(neg) as f:
    NEGATIVES.extend([x.strip('\n') for x in f.readlines()])

for i in POSITIVES:
    try:
        urllib2.urlopen(i)
    except urllib2.HTTPError, e:
        POSITIVES.remove(i)
    except urllib2.URLError, e:
        POSITIVES.remove(i)
for i in NEGATIVES:
    try:
        urllib2.urlopen(i)
    except urllib2.HTTPError, e:
        NEGATIVES.remove(i)
    except urllib2.URLError, e:
        NEGATIVES.remove(i)

concept_name = 'car'
for positive_example in POSITIVES:
    try:
        clarifai.positive(positive_example, concept_name)
    except socket.gaierror:
        print 'ignoring failed address lookup for: ', positive_example  
for negative_example in NEGATIVES:
    try:
        clarifai.negative(negative_example, concept_name)
    except socket.gaierror:
        print 'ignoring failed address lookup for: ', negative_example
clarifai.train(concept_name)

concept_name = 'automobile'
for positive_example in POSITIVES:
  clarifai.positive(positive_example, concept_name)
clarifai.train(concept_name)
concept_name = 'auto'
for positive_example in POSITIVES:
  clarifai.positive(positive_example, concept_name)
clarifai.train(concept_name)
concept_name = 'vehicle'
for positive_example in POSITIVES:
  clarifai.positive(positive_example, concept_name)
clarifai.train(concept_name)

#XXXXXXXXXXXXXXXXXXX BIKE XXXXXXXXXXXXXXXXXXXXXXXXXXXXX
POSITIVES = []
pos=p+"/images/bikes.txt"
with open(pos) as f:
    POSITIVES = [x.strip('\n') for x in f.readlines()]

NEGATIVES = []
neg=p+"/images/cars.txt"
with open(neg) as f:
    NEGATIVES.extend([x.strip('\n') for x in f.readlines()])
    
neg=p+"/images/buses.txt"
with open(neg) as f:
    NEGATIVES.extend([x.strip('\n') for x in f.readlines()])
    
neg=p+"/images/motorbikes.txt"
with open(neg) as f:
    NEGATIVES.extend([x.strip('\n') for x in f.readlines()])

for i in POSITIVES:
    try:
        urllib2.urlopen(i)
    except urllib2.HTTPError, e:
        POSITIVES.remove(i)
    except urllib2.URLError, e:
        POSITIVES.remove(i)
for i in NEGATIVES:
    try:
        urllib2.urlopen(i)
    except urllib2.HTTPError, e:
        NEGATIVES.remove(i)
    except urllib2.URLError, e:
        NEGATIVES.remove(i)

concept_name = 'bike'
for positive_example in POSITIVES:
  clarifai.positive(positive_example, concept_name)
for negative_example in NEGATIVES:
  clarifai.negative(negative_example, concept_name)
clarifai.train(concept_name)

concept_name = 'bicycle'
for positive_example in POSITIVES:
  clarifai.positive(positive_example, concept_name)
for negative_example in NEGATIVES:
  clarifai.negative(negative_example, concept_name)
clarifai.train(concept_name)

concept_name = 'vehicle'
for positive_example in POSITIVES:
  clarifai.positive(positive_example, concept_name)
clarifai.train(concept_name)

#XXXXXXXXXXXXXXXXXXX BUSES XXXXXXXXXXXXXXXXXXXXXXXXXXXXX
POSITIVES = []
pos=p+"/images/buses.txt"
with open(pos) as f:
    POSITIVES = [x.strip('\n') for x in f.readlines()]

NEGATIVES = []
neg=p+"/images/cars.txt"
with open(neg) as f:
    NEGATIVES.extend([x.strip('\n') for x in f.readlines()])
    
neg=p+"/images/bikes.txt"
with open(neg) as f:
    NEGATIVES.extend([x.strip('\n') for x in f.readlines()])
    
neg=p+"/images/motorbikes.txt"
with open(neg) as f:
    NEGATIVES.extend([x.strip('\n') for x in f.readlines()])

for i in POSITIVES:
    try:
        urllib2.urlopen(i)
    except urllib2.HTTPError, e:
        POSITIVES.remove(i)
    except urllib2.URLError, e:
        POSITIVES.remove(i)
for i in NEGATIVES:
    try:
        urllib2.urlopen(i)
    except urllib2.HTTPError, e:
        NEGATIVES.remove(i)
    except urllib2.URLError, e:
        NEGATIVES.remove(i)

concept_name = 'bus'
for positive_example in POSITIVES:
  clarifai.positive(positive_example, concept_name)
for negative_example in NEGATIVES:
  clarifai.negative(negative_example, concept_name)
clarifai.train(concept_name)

concept_name = 'vehicle'
for positive_example in POSITIVES:
  clarifai.positive(positive_example, concept_name)
clarifai.train(concept_name)

#XXXXXXXXXXXXXXXXXXX MOTORBIKE XXXXXXXXXXXXXXXXXXXXXXXXXXXXX
POSITIVES = []
pos=p+"/images/motorbikes.txt"
with open(pos) as f:
    POSITIVES = [x.strip('\n') for x in f.readlines()]

NEGATIVES = []
neg=p+"/images/bikes.txt"
with open(neg) as f:
    NEGATIVES.extend([x.strip('\n') for x in f.readlines()])
    
neg=p+"/images/buses.txt"
with open(neg) as f:
    NEGATIVES.extend([x.strip('\n') for x in f.readlines()])
    
neg=p+"/images/cars.txt"
with open(neg) as f:
    NEGATIVES.extend([x.strip('\n') for x in f.readlines()])

for i in POSITIVES:
    try:
        urllib2.urlopen(i)
    except urllib2.HTTPError, e:
        POSITIVES.remove(i)
    except urllib2.URLError, e:
        POSITIVES.remove(i)
for i in NEGATIVES:
    try:
        urllib2.urlopen(i)
    except urllib2.HTTPError, e:
        NEGATIVES.remove(i)
    except urllib2.URLError, e:
        NEGATIVES.remove(i)

concept_name = 'motorbike'
for positive_example in POSITIVES:
  clarifai.positive(positive_example, concept_name)
for negative_example in NEGATIVES:
  clarifai.negative(negative_example, concept_name)
clarifai.train(concept_name)

concept_name = 'motorcycle'
for positive_example in POSITIVES:
  clarifai.positive(positive_example, concept_name)
for negative_example in NEGATIVES:
  clarifai.negative(negative_example, concept_name)
clarifai.train(concept_name)

concept_name = 'vehicle'
for positive_example in POSITIVES:
  clarifai.positive(positive_example, concept_name)
clarifai.train(concept_name)

#XXXXXXXXXXXXXXXXXXX PEOPLE XXXXXXXXXXXXXXXXXXXXXXXXXXXXX
POSITIVES = []
pos=p+"/images/people.txt"
with open(pos) as f:
    POSITIVES = [x.strip('\n') for x in f.readlines()]

NEGATIVES = []
neg=p+"/images/nobody.txt"
with open(neg) as f:
    NEGATIVES.extend([x.strip('\n') for x in f.readlines()])

for i in POSITIVES:
    try:
        urllib2.urlopen(i)
    except urllib2.HTTPError, e:
        POSITIVES.remove(i)
    except urllib2.URLError, e:
        POSITIVES.remove(i)
for i in NEGATIVES:
    try:
        urllib2.urlopen(i)
    except urllib2.HTTPError, e:
        NEGATIVES.remove(i)
    except urllib2.URLError, e:
        NEGATIVES.remove(i)
        
concept_name = 'people'
for positive_example in POSITIVES:
  clarifai.positive(positive_example, concept_name)
for negative_example in NEGATIVES:
  clarifai.negative(negative_example, concept_name)
clarifai.train(concept_name)

#XXXXXXXXXXXXXXXXXXX USAGE XXXXXXXXXXXXXXXXXXXXXXXXXXXXX

EXAMPLES = [
  'https://blog-blogmediainc.netdna-ssl.com/SportsBlogcom/filewarehouse/37676/4b8c8d0728f0fb6e78b1071445770fab.jpg',
  'http://www.africacradle.com/wp-content/uploads/2015/08/article-2441512-02650200000005DC-411_634x380.jpg'
]

NOT = [
  'https://clarifai-test.s3.amazonaws.com/2141620332_2b741028b3.jpg',
  'https://clarifai-test.s3.amazonaws.com/grateful_dead230582_15-52.jpg'
]

for test in EXAMPLES + NOT:
  result = clarifai.predict(test, 'car')
  ans = 'ACCEPTED' if (result['urls'][0]['score']>0.7) else 'REJECTED'
  print result['status']['message'], "%0.3f" % result['urls'][0]['score'], ans, result['urls'][0]['url']
