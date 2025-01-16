works = []
for dx in range(-5,6):
    for dy in range(-5,6):
        if dx*dx+dy*dy<=20:
            works += [(dx,dy)]
works.sort(key=lambda a:a[0]*a[0]+a[1]*a[1])
#mop
#weighing scores
a = [(-1, -1), (0, -1), (1, -1), (-1, 0), (1, 0), (-1, 1), (0, 1), (1, 1)]
for d in a:
    asdf = []
    s = ''
    for dx in range(-1, 2):
        for dy in range(-1, 2):
            if (dx+d[0])**2 + (dy+d[1])**2 > 2:
                ind = works.index((dx+d[0],dy+d[1]))
                # s += f'\t\t//if we move to [{d[0]}, {d[1]}] (index {a.index(d)}), then we will be able to attack [{dx+d[0]}, {dy+d[1]}] (index {ind}), so check that too\n'
                s += f'\t\tif (attackScores[{ind}] > allmax[{a.index(d)}])' + ' {\n'
                s += f'\t\t\tallmax[{a.index(d)}] = attackScores[{ind}];\n'
                s += f'\t\t\tallx[{a.index(d)}] = {dx+d[0]};\n'
                s += f'\t\t\tally[{a.index(d)}] = {dy+d[1]};\n'
                s += f'\t\t\tallswing[{a.index(d)}] = false;\n'
                s += '\t\t}\n'
                # asdf += [works.index((dx+d[0],dy+d[1]))]
    # print(d,asdf)
    print(s,end='')

#calculating scores
# for i in range(25):
#     print("""\t\tloc = G.me.translate("""+str(works[i][0])+""", """+str(works[i][1])+""");
#         if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
#             if (G.rc.canSenseRobotAtLocation(loc)) {
#                 //if it's an opponent, they get -1 paint
#                 //if it's an ally, they go from -2 to -1 paint
#                 //in both cases we gain 1 paint
#                 //can't be a tower because it has to be painted
#                 RobotInfo bot = G.rc.senseRobotAtLocation(loc);
#                 // mopScores["""+str(i)+"""] += (1 - bot.paintAmount / (double) bot.type.paintCapacity) * 10;
#                 mopScores["""+str(i)+"""] += 11 + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint());
#                 if (bot.getType() == UnitType.MOPPER) {
#                     //double passive paint loss on moppers
#                     mopScores["""+str(i)+"""]++;
#                 }
#             }
#             mopScores["""+str(i)+"""] += 5;
#         }""")

print()
print()
print()
#swings
#calculating scores
a = [(-1, -1), (0, -1), (1, -1), (-1, 0), (1, 0), (-1, 1), (0, 1), (1, 1), (0, 0)]
a2 = [
    [(0, -1), (0, -2), (-1, -1), (-1, -2), (1, -1), (1, -2)],
    [(-1, 0), (-2, 0), (-1, -1), (-2, -1), (-1, 1), (-2, 1)],
    [(1, 0), (2, 0), (1, 1), (2, 1), (1, -1), (2, -1)],
    [(0, 1), (0, 2), (1, 1), (1, 2), (-1, 1), (-1, 2)]
]
s = ''
for i in range(36):
    s+='\t\t\tswingScores['+str(i)+'] = 0;\n'
for d in a:
    for d2 in a2:
        for i in d2:
            s += f'\t\t\tif (opponentRobotsString.indexOf("["+(G.me.x{""if d[0]+i[0]<0 else "+"}{d[0]+i[0]})+", "+(G.me.y{""if d[1]+i[1]<0 else "+"}{d[1]+i[1]})+"]") != -1)' + ' {\n'
            s += f'\t\t\t\tswingScores[{a.index(d)*4+a2.index(d2)}] += 5;\n'
            s += '\t\t\t}\n'
print(s)

#weighing scores
s=''
for d2 in a2:
    s += f'\t\tif (swingScores[{32+a2.index(d2)}] > cmax)' + ' {\n'
    s += f'\t\t\tcmax = swingScores[{32+a2.index(d2)}];\n'
    s += f'\t\t\tcx = {a.index(d2[0])};\n'
    s += '\t\t\tswing = true;\n'
    s += '\t\t}\n'
print(s)
s=''
for d in a[:-1]:
    for d2 in a2:
        s += f'\t\tif (swingScores[{a.index(d)*4+a2.index(d2)}] > allmax[{a.index(d)}])' + ' {\n'
        s += f'\t\t\tallmax[{a.index(d)}] = swingScores[{a.index(d)*4+a2.index(d2)}];\n'
        s += f'\t\t\tallx[{a.index(d)}] = {a.index(d2[0])};\n'
        s += f'\t\t\tallswing[{a.index(d)}] = true;\n'
        s += '\t\t}\n'
print(s)