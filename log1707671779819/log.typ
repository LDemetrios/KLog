#import "@preview/tablex:0.0.8": *

#let subfolder = ""

#let header = read("header.txt").split("\n").filter(it => it != "")

#let thread-log(name) = {
  let table = read(name).split("\n").filter(it => it != "").map(row => {
    row.replace("\\n", "\n").split("\\;")
  })

  table.map(
    row => {
      let rowtype = row.at(0)
      if rowtype == "open" {
        (time: int(row.at(2)), data: (
          rowtype: "open",
          depth: int(row.at(1)),
          line: int(row.at(3)),
          class: row.at(4),
          method: row.at(5),
          args: row.slice(6),
        ))
      } else if rowtype == "close" {
        (time: row.at(2), data: (
          rowtype: "close",
          depth: int(row.at(1)),
          resulttype: row.at(3),
          result: row.at(4),
        ))
      } else if rowtype == "value" {
        (time: int(row.at(1)), data: (
          rowtype: "value",
          line: int(row.at(2)),
          name: row.at(3),
          value: row.at(4),
        ))
      } else if rowtype == "uncaught" {
        (time: int(row.at(1)), data: (rowtype: "value", throwable: row.at(2)))
      }
    },
  )
}

#let compose-thread-log(list) = {
  let prev = none
  let array = ()
  let depth = 0
  let indent(n) = text(fill:luma(127), for i in range(n) {
    `|   `
  })
  for el in list {
    let content = {
      if el.data.rowtype == "open" {
        indent(el.data.depth)
        raw("L" + str(el.data.line) + " -> ")
        raw(
          el.data.class + "::" + el.data.method + "(" + el.data.args.join() + ") {",
        )

        linebreak()
        depth = el.data.depth
      } else if el.data.rowtype == "close" {
        indent(el.data.depth)
        if el.data.resulttype == "returned" {
          `return `
          el.data.result
          linebreak()
          indent(el.data.depth - 1)
          `}`
          linebreak()
        } else if el.data.resulttype == "threw" {
          `throw `
          el.data.throwable
          linebreak()
          indent(el.data.depth - 1)
          `}`
        }
        depth = el.data.depth - 1
      } else if el.data.rowtype == "value" {
        indent(depth)
        raw(
          "L" + str(el.data.line) + " -> " + el.data.name + " = " + el.data.value,
        )
      }
    }
    array.push(content)
  }
  array
}

#let logs = range(header.len()).map(ind => {
  let log = thread-log(header.at(ind))
  log.map(it => (thread: ind, time: it.time, data: it.data))
}).map(compose-thread-log)

#set page(width: auto)

#tablex(columns: 0, auto-hlines: false, ..logs.at(0))
