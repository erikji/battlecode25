distanceSquared = lambda x,y:x**2+y**2

print('switch (radiusSquared) {')
for i in range(20):
    print(f'\tcase {i}:')
    print('\t\treturn new MapLocation[] {')
    for dx in range(-i, i+1):
        for dy in range(-i, i+1):
            if dx==0 and dy==0:
                print('\t\t\tcenter,')
            elif distanceSquared(dx,dy)<=i:
                print('\t\t\tnew MapLocation(center.x'+(('+'if dx > 0 else '')+str(dx) if dx!=0 else '')+',center.y'+(('+'if dy > 0 else '')+str(dy) if dy!=0 else '')+'),')
    print('\t\t};')
print('};')