done = function(summary, latency, requests)

    io.write("--------------------------\n")
    local durations = summary.duration / 1000000    -- total time, second
    local errors = summary.errors.status            -- http status is not 200，300
    local total = summary.requests               -- total requests
    local valid = total - errors                   -- valid requests = total requests - error requests

    io.write("Durations:       " .. string.format("%.2f", durations) .. "s" .. "\n")
    io.write("Requests:        " .. summary.requests .. "\n")
    io.write("Avg RT:          " .. string.format("%.2f", latency.mean / 1000) .. "ms" .. "\n")
    io.write("Max RT:          " .. (latency.max / 1000) .. "ms" .. "\n")
    io.write("Min RT:          " .. (latency.min / 1000) .. "ms" .. "\n")
    io.write("Error requests:  " .. errors .. "\n")
    io.write("Valid requests:  " .. valid .. "\n")
    io.write("MAX-QPS/THREAD:  " .. string.format("%.2f", requests.max) .. "\n")
    io.write("AVG-QPS:         " .. string.format("%.2f", valid / durations) .. "\n")
    io.write("--------------------------\n")

end
