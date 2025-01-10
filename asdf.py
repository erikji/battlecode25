a,b,c=map(float,input().split())
import math
try:
	print((-b+math.sqrt(b*b-4*a*c))/2/a)
except:
	pass
try:
	print((-b-math.sqrt(b*b-4*a*c))/2/a)
except:
	pass
