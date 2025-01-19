from range20 import works
#mop
#weighing scores
a = [(-1, -1), (0, -1), (1, -1), (1, 0), (1, 1), (0, 1), (-1, 1), (-1, 0)]
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
for i in range(25):
    print("""\t\tloc = G.me.translate("""+str(works[i][0])+""", """+str(works[i][1])+""");
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores["""+str(i)+"""] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    //treat freezing bot equivalent to gaining 20 paint
                    attackScores["""+str(i)+"""] += 100;
                }
            }
            if (target.distanceSquaredTo(loc) <= 8) {
                attackScores["""+str(i)+"""] += 50;
            }
            attackScores["""+str(i)+"""] += 25;
        }""")

            # if (target.distanceSquaredTo(loc) <= 8) {
            #     attackScores["""+str(i)+"""] += 50;
            # }
print()
print()
print()
#swings
#calculating scores
a = [(-1, -1), (0, -1), (1, -1), (1, 0), (1, 1), (0, 1), (-1, 1), (-1, 0), (0, 0)]
a2 = [
    [(0, -1), (0, -2), (-1, -1), (-1, -2), (1, -1), (1, -2)],
    [(-1, 0), (-2, 0), (-1, -1), (-2, -1), (-1, 1), (-2, 1)],
    [(1, 0), (2, 0), (1, 1), (2, 1), (1, -1), (2, -1)],
    [(0, 1), (0, 2), (1, 1), (1, 2), (-1, 1), (-1, 2)]
]
s = ['\t\t\tMapLocation loc;\n']
for d in a:
    for d2 in a2:
        for i in d2:
            try:
                ind = s.index(f'\t\t\tloc = G.me.translate({d[0]+i[0]}, {d[1]+i[1]});\n')
                if ind < 0:
                    raise Exception()
                s.insert(ind+3, f'\t\t\t\tswingScores[{a.index(d)*4+a2.index(d2)}] += Math.min(5, bot.paintAmount) * 7;\n')
            except:
                s.append(f'\t\tloc = G.me.translate({d[0]+i[0]}, {d[1]+i[1]});\n')
                s.append(f'\t\tif (G.opponentRobotsString.indexOf(loc.toString()) != -1)' + ' {\n')
                s.append(f'\t\t\tRobotInfo bot = G.rc.senseRobotAtLocation(loc);\n')
                s.append(f'\t\t\tswingScores[{a.index(d)*4+a2.index(d2)}] += Math.min(5, bot.paintAmount) * 7; //7 because the cooldown is lower for swing\n')
                s.append('\t\t}\n')
# s = s.split('\n')
print(''.join(s))

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