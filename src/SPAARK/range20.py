# worksX,worksY = [],[]
works = []
for dx in range(-5,6):
    for dy in range(-5,6):
        if dx*dx+dy*dy<=20:
            works += [(dx,dy)]
            # worksX+=[dx]
            # worksY+=[dy]
works.sort(key=lambda a:a[0]*a[0]+a[1]*a[1])
print(list(zip(*works)))