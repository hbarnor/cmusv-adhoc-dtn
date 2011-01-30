/*
 *    Copyright 2006 Intel Corporation
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

#ifndef __SERIALIZEHELPERS_H__
#define __SERIALIZEHELPERS_H__

#include <vector>

#include "Serialize.h"

namespace oasys {

//----------------------------------------------------------------------------
template<typename _type> void
serialize_basic_vector(oasys::SerializeAction* action,
                       std::string             name,
                       std::vector<_type>*     v)
{
    name += ".size";

    u_int32_t size;
    size = v->size();
    action->process(name.c_str(), &size);
    
    if (action->error())
        return;

    v->resize(size);
    
    for (u_int32_t i=0; i<size; ++i)
    {
        action->process(name.c_str(), &((*v)[i]));
        if (action->error())
            return;
    }
}

} // namespace oasys

#endif /* __SERIALIZEHELPERS_H__ */
