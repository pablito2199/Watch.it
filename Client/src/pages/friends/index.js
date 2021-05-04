import { CalendarOutline as Calendar, LocationMarkerOutline as Location } from '@graywolfai/react-heroicons'
import { Shell, Separator } from '../../components'

import { useUser } from '../../hooks'

export default function Profile() {
    const { user, createUser, updateUser } = useUser()

    return <Shell>
        <div className='mx-auto w-full max-w-screen-2xl p-8'>
            <img
                style={{ height: '36rem' }}
                src={user.picture}
                alt={user.name}
                className='absolute top-2 left-0 right-0 w-full object-cover filter blur transform scale-105'
            />
            <Header user={user} />
            <PendingFriendships user={user} />
            <AcceptedFriendships user={user} />
        </div>
    </Shell>
}

function Header({ user }) {
    return <header className='mt-96 relative flex pb-8 mb-8'>
        <img style={{ aspectRatio: '2/3' }}
            src={user.picture}
            alt={user.name}
            className='absolute w-64 rounded-full shadow-xl z-20' />
        <hgroup className='ml-12 flex-1 mt-28'>
            <h1 className={`bg-black bg-opacity-50 backdrop-filter backdrop-blur 
                                      text-right text-white text-6xl font-bold
                                      p-6`}>
                {user.name}
            </h1>
            <Info user={user} />
        </hgroup>
    </header>
}
function Info({ user }) {
    return <div className='flex justify-between'>
        <div className='ml-60 flex'>
            <Calendar className='h-12 w-12 mt-2' />
            <span className={`self-centerblock text-2xl font-semibold text-black w-full py-4 text-right`}>
                {
                    user.birthday && <>{user.birthday.day}/{user.birthday.month}/{user.birthday.year}</>
                }
            </span>
        </div>
        <div className='flex ml-60'>
            <Location className='h-12 w-12 mt-2' />
            <span className={`self-centerblock text-2xl font-semibold text-black w-full py-4 text-right`}>
                {user.country}
            </span>
        </div>
        <span className={`block text-3xl font-semibold text-black w-full px-8 py-4 text-right`}>
            {user.email}
        </span>
    </div>
}

function PendingFriendships({ user }) {
    return <>
        <h2 className='mt-16 font-bold text-2xl'>Solicitudes de amistad</h2>
        <Separator />
        <div>
            
        </div>
    </>
}

function AcceptedFriendships({ user }) {
    return <>
        <h2 className='mt-16 font-bold text-2xl'>Amigos</h2>
        <Separator />
        <div>
            
        </div>
    </>
}